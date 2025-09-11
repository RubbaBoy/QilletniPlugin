package dev.qilletni.intellij.library;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.AdditionalLibraryRootsProvider;
import com.intellij.openapi.roots.SyntheticLibrary;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Provides SyntheticLibrary nodes for Qilletni libs when no real Project Libraries exist.
 * When Option B (real libraries) is active and present in the project, this provider returns empty
 * to avoid duplicate External Libraries entries.
 */
public final class QilletniAdditionalLibraryRootsProvider extends AdditionalLibraryRootsProvider {
    private static final String PREFIX = "Qilletni: ";

    @Override
    public @NotNull Collection<SyntheticLibrary> getAdditionalProjectLibraries(@NotNull Project project) {
        // If project already has real Qilletni libraries, skip synthetic ones
        var table = LibraryTablesRegistrar.getInstance().getLibraryTable(project);
        for (var lib : table.getLibraries()) {
            var name = lib.getName();
            if (name != null && name.startsWith(PREFIX)) {
                return List.of();
            }
        }

        var libs = QilletniLibraryManager.getInstance().getInstalled();
        List<SyntheticLibrary> result = new ArrayList<>();
        for (var l : libs) {
            List<VirtualFile> sources = new ArrayList<>();
            List<VirtualFile> binaries = new ArrayList<>();

            var src = l.srcRoot();
            if (src != null && src.isValid()) sources.add(src);

            var nativeJar = l.nativeJarRoot();
            if (nativeJar != null && nativeJar.isValid()) binaries.add(nativeJar);

            if (!sources.isEmpty() || !binaries.isEmpty()) {
                var presentable = l.name() + (l.version() != null ? " " + l.version().getVersionString() : "");
                // Use binary synthetic library class that supports both roots
                result.add(new QilletniBinarySyntheticLibrary(
                        l.name(),
                        presentable,
                        binaries,
                        sources
                ));
            } else {
                System.out.println("[LIB_PROVIDER] skipping library (no roots): name=" + l.name());
            }
        }
        return result;
    }

}
