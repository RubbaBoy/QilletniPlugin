package dev.qilletni.intellij.library;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.AdditionalLibraryRootsProvider;
import com.intellij.openapi.roots.SyntheticLibrary;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * Placeholder provider that exposes no additional libraries yet.
 * This wires the extension point without changing behavior.
 */
public final class QilletniAdditionalLibraryRootsProvider extends AdditionalLibraryRootsProvider {
    @Override
    public @NotNull Collection<SyntheticLibrary> getAdditionalProjectLibraries(@NotNull Project project) {
        return List.of();
    }
}
