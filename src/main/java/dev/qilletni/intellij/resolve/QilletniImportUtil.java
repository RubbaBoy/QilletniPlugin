package dev.qilletni.intellij.resolve;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import dev.qilletni.intellij.QilletniFile;
import dev.qilletni.intellij.psi.QilletniImportFile;
import dev.qilletni.intellij.psi.QilletniTypes;

import java.io.File;
import java.util.*;

/**
 * Utilities to collect import information for a Qilletni file and resolve project-local imported files.
 * - Supports imports like: import "directFile.ql" [AS alias]
 * - Library imports like: import "libName:path/in/lib.ql" are recognized but not resolved yet.
 */
final class QilletniImportUtil {
    private QilletniImportUtil() {}

    // Resolver chain for library-style imports (e.g., "libName:path/in/lib.ql").
    private static final List<QilletniLibraryPathResolver> LIB_RESOLVERS = List.of(
            new dev.qilletni.intellij.resolve.impl.NoopLibraryPathResolver(),
            new dev.qilletni.intellij.resolve.impl.LibraryRootsPathResolver()
    );

    static List<VirtualFile> getFilesForQualifier(QilletniFile contextFile, String qualifier) {
        Map<String, List<VirtualFile>> byAlias = collectAliasedImports(contextFile);
        return new ArrayList<>(byAlias.getOrDefault(qualifier, Collections.emptyList()));
    }

    static List<VirtualFile> getAllImportedFiles(QilletniFile contextFile) {
        Set<VirtualFile> set = new LinkedHashSet<>();
        for (List<VirtualFile> vfs : collectAliasedImports(contextFile).values()) {
            set.addAll(vfs);
        }
        set.addAll(collectUnaliasedImports(contextFile));
        return new ArrayList<>(set);
    }

    static List<VirtualFile> getProjectLocalImports(QilletniFile contextFile) {
        Set<VirtualFile> set = new LinkedHashSet<>(collectUnaliasedImports(contextFile));
        for (List<VirtualFile> vfs : collectAliasedImports(contextFile).values()) set.addAll(vfs);
        return new ArrayList<>(set);
    }

    // Exposed for alias resolver
    static Map<String, List<VirtualFile>> collectAliasedImports(QilletniFile file) {
        Map<String, List<VirtualFile>> res = new LinkedHashMap<>();
        Project project = file.getProject();
        for (var imp : PsiTreeUtil.findChildrenOfType(file, QilletniImportFile.class)) {
            String raw = findStringLiteralText(imp);
            if (raw == null) continue;
            String alias = findAlias(imp);
            if (alias == null) continue; // this method only collects aliased imports

            if (isLibraryPath(raw)) {
                List<VirtualFile> files = resolveLibraryFiles(project, raw);
                if (!files.isEmpty()) {
                    res.computeIfAbsent(alias, k -> new ArrayList<>()).addAll(files);
                }
            } else {
                var vf = resolveProjectLocalFile(project, raw);
                if (vf != null) {
                    res.computeIfAbsent(alias, k -> new ArrayList<>()).add(vf);
                }
            }
        }
        return res;
    }

    private static List<VirtualFile> collectUnaliasedImports(QilletniFile file) {
        List<VirtualFile> res = new ArrayList<>();
        Project project = file.getProject();
        for (var imp : PsiTreeUtil.findChildrenOfType(file, QilletniImportFile.class)) {
            String raw = findStringLiteralText(imp);
            if (raw == null) continue;
            String alias = findAlias(imp);
            if (alias != null) continue; // skip aliased; this method collects only unaliased
            if (isLibraryPath(raw)) {
                res.addAll(resolveLibraryFiles(project, raw));
            } else {
                var vf = resolveProjectLocalFile(project, raw);
                if (vf != null) res.add(vf);
            }
        }
        return res;
    }

    private static String findStringLiteralText(QilletniImportFile imp) {
        // STRING token holds the path, includes quotes; strip them
        for (PsiElement c = imp.getFirstChild(); c != null; c = c.getNextSibling()) {
            if (c.getNode() != null && c.getNode().getElementType() == QilletniTypes.STRING) {
                String t = c.getText();
                if (t.length() >= 2 && (t.startsWith("\"") && t.endsWith("\""))) {
                    String inner = t.substring(1, t.length() - 1);
                    // Library imports look like "lib:foo/bar.ql" — we recognize but don't resolve them yet
                    return inner;
                }
                return t;
            }
        }
        return null;
    }

    private static String findAlias(QilletniImportFile imp) {
        for (PsiElement c = imp.getFirstChild(); c != null; c = c.getNextSibling()) {
            if (c.getNode() != null && c.getNode().getElementType() == QilletniTypes.ID) {
                return c.getText();
            }
        }
        return null;
    }

    private static boolean isLibraryPath(String raw) {
        int colon = raw.indexOf(':');
        return colon > 0; // "name:path"
    }

    private static List<VirtualFile> resolveLibraryFiles(Project project, String raw) {
        if (project == null || raw == null) return List.of();
        for (var resolver : LIB_RESOLVERS) {
            try {
                if (resolver != null && resolver.supports(raw)) {
                    List<VirtualFile> files = resolver.resolve(project, raw);
                    return files != null ? files : List.of();
                }
            } catch (Throwable ignored) {
                // defensive: never let a resolver break import collection
            }
        }
        return List.of();
    }

    private static VirtualFile resolveProjectLocalFile(Project project, String importPathLiteral) {
        if (isLibraryPath(importPathLiteral)) return null; // library paths handled elsewhere
        String basePath = project.getBasePath();
        if (basePath == null) return null;
        File ioFile = new File(basePath, importPathLiteral);
        return LocalFileSystem.getInstance().findFileByIoFile(ioFile);
    }
}
