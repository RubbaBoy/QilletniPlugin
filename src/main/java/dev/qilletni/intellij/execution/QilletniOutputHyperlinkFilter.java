package dev.qilletni.intellij.execution;

import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.HyperlinkInfo;
import com.intellij.execution.filters.OpenFileHyperlinkInfo;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Detects /path/file.ql:line patterns and links to source. */
public final class QilletniOutputHyperlinkFilter implements Filter {
    private static final Pattern PATTERN = Pattern.compile("(/[^:\n]+\\.ql):(\\d+)");
    private final Project project;

    public QilletniOutputHyperlinkFilter(Project project) { this.project = project; }

    @Override
    public @Nullable Result applyFilter(@NotNull String line, int entireLength) {
        Matcher m = PATTERN.matcher(line);
        if (!m.find()) return null;
        var path = m.group(1);
        var lineNo = Integer.parseInt(m.group(2));
        var vf = LocalFileSystem.getInstance().findFileByPath(path);
        if (vf == null) return null;
        HyperlinkInfo link = new OpenFileHyperlinkInfo(project, vf, Math.max(0, lineNo - 1));
        int start = entireLength - line.length() + m.start(1);
        int end = entireLength - line.length() + m.end(2);
        return new Result(start, end, link);
    }
}
