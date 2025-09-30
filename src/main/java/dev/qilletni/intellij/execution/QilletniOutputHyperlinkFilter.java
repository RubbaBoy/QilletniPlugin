package dev.qilletni.intellij.execution;

import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.HyperlinkInfo;
import com.intellij.execution.filters.OpenFileHyperlinkInfo;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Detects file references in console output and turns them into hyperlinks.
 * Supported forms:
 * - POSIX absolute path: /path/to/file.ext:line[:col]
 * - Windows absolute path: C:\\path\\to\\file.ext:line[:col]
 * - Java stacktrace: (File.java:line)
 */
public final class QilletniOutputHyperlinkFilter implements Filter {
    private static final Pattern POSIX = Pattern.compile("(?<![\\u00A0-\\uFFFF])(/[^\\s:]+):([0-9]+)(?::([0-9]+))?");
    private static final Pattern WINDOWS = Pattern.compile("([A-Za-z]:\\\\[^\\s:]+):([0-9]+)(?::([0-9]+))?");
    private static final Pattern JAVA_STACKTRACE = Pattern.compile("\\(([^()/:\\\\]+\\.[A-Za-z0-9]+):([0-9]+)\\)");

    private final Project project;

    public QilletniOutputHyperlinkFilter(Project project) { this.project = project; }

    @Override
    public @Nullable Result applyFilter(@NotNull String line, int entireLength) {
        int base = entireLength - line.length();
        var items = new ArrayList<ResultItem>(4);
        // Absolute POSIX paths
        collectPathMatches(POSIX.matcher(line), base, items, true);
        // Absolute Windows paths
        collectPathMatches(WINDOWS.matcher(line), base, items, true);
        // Java stacktrace filename-only matches: resolve within project
        collectJavaStacktraceMatches(JAVA_STACKTRACE.matcher(line), base, items);
        if (items.isEmpty()) return null;
        return new Result(items);
    }

    private void collectPathMatches(Matcher m, int base, List<ResultItem> out, boolean absoluteOnly) {
        while (m.find()) {
            var path = m.group(1);
            int lineNo = parseInt(m.group(2), 1);
            int colNo = parseInt(m.group(3), 1);
            VirtualFile vf = LocalFileSystem.getInstance().findFileByPath(path);
            if (vf == null) continue;
            HyperlinkInfo link = new OpenFileHyperlinkInfo(project, vf, Math.max(0, lineNo - 1), Math.max(0, colNo - 1));
            int g3end = m.end(3);
            int end = g3end != -1 ? base + g3end : base + m.end(2);
            out.add(new ResultItem(base + m.start(1), end, link));
        }
    }

    private void collectJavaStacktraceMatches(Matcher m, int base, List<ResultItem> out) {
        while (m.find()) {
            var fileName = m.group(1);
            int lineNo = parseInt(m.group(2), 1);
            var files = FilenameIndex.getVirtualFilesByName(project, fileName, GlobalSearchScope.projectScope(project));
            if (files.isEmpty()) continue;
            // Prefer a source file under content roots; just take the first for now
            VirtualFile vf = files.iterator().next();
            HyperlinkInfo link = new OpenFileHyperlinkInfo(project, vf, Math.max(0, lineNo - 1));
            out.add(new ResultItem(base + m.start(1) - 1, base + m.end(2) + 1, link)); // include parentheses area
        }
    }

    private static int parseInt(String s, int def) {
        try { return s == null ? def : Integer.parseInt(s); } catch (NumberFormatException e) { return def; }
    }
}
