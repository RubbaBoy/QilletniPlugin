package dev.qilletni.intellij.resolve.impl;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.AdditionalLibraryRootsProvider;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import dev.qilletni.intellij.resolve.QilletniLibraryPathResolver;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Resolver that consults AdditionalLibraryRootsProvider and project/application libraries to resolve
 * library-style imports like "libName:path/in/lib.ql". If no matching libraries are attached,
 * returns an empty list (preserving behavior).
 */
public final class LibraryRootsPathResolver implements QilletniLibraryPathResolver {
    @Override
    public boolean supports(String rawImportPath) {
        if (rawImportPath == null) return false;
        int colon = rawImportPath.indexOf(':');
        return colon > 0 && colon < rawImportPath.length() - 1;
    }

    @Override
    public List<VirtualFile> resolve(Project project, String rawImportPath) {
        if (project == null || rawImportPath == null) return List.of();
        int colon = rawImportPath.indexOf(':');
        if (colon <= 0 || colon >= rawImportPath.length() - 1) return List.of();

        String libName = rawImportPath.substring(0, colon).trim();
        String relPath = rawImportPath.substring(colon + 1).trim();
        if (relPath.startsWith("/")) relPath = relPath.substring(1);
        if (libName.isEmpty() || relPath.isEmpty()) return List.of();

        Set<VirtualFile> matches = new LinkedHashSet<>();
        // 1) Synthetic libraries from all providers
        for (var provider : AdditionalLibraryRootsProvider.EP_NAME.getExtensionList()) {
            try {
                var libs = provider.getAdditionalProjectLibraries(project);
                if (libs == null) continue;
                final String relPathFinal = relPath;
                libs.forEach(synth -> {
                    collectFromRoots(matches, synth.getSourceRoots(), relPathFinal);
                    collectFromRoots(matches, synth.getBinaryRoots(), relPathFinal);
                });
            } catch (Throwable ignored) {
            }
        }

        // 2) Standard libraries (project and application level)
        collectFromLibraryTable(project, libName, relPath, matches);
        collectFromLibraryTable(null, libName, relPath, matches); // application-level

        return new ArrayList<>(matches);
    }

    private static void collectFromLibraryTable(Project project, @NotNull String libName, @NotNull String relPath, Set<VirtualFile> out) {
        var registrar = LibraryTablesRegistrar.getInstance();
        var table = project != null ? registrar.getLibraryTable(project) : registrar.getLibraryTable();
        if (table == null) return;
        for (Library lib : table.getLibraries()) {
            if (lib == null) continue;
            String name = lib.getName();
            if (name == null || !name.equalsIgnoreCase(libName)) continue;
            collectFromRoots(out, lib.getFiles(OrderRootType.CLASSES), relPath);
            collectFromRoots(out, lib.getFiles(OrderRootType.SOURCES), relPath);
        }
    }

    private static void collectFromRoots(Set<VirtualFile> out, VirtualFile[] roots, String relPath) {
        if (roots == null) return;
        for (var root : roots) {
            if (root == null) continue;
            var child = VfsUtilCore.findRelativeFile(relPath, root);
            if (child != null && child.exists() && !child.isDirectory()) out.add(child);
        }
    }

    private static void collectFromRoots(Set<VirtualFile> out, Collection<VirtualFile> roots, String relPath) {
        if (roots == null) return;
        for (var root : roots) {
            if (root == null) continue;
            var child = VfsUtilCore.findRelativeFile(relPath, root);
            if (child != null && child.exists() && !child.isDirectory()) out.add(child);
        }
    }
}
