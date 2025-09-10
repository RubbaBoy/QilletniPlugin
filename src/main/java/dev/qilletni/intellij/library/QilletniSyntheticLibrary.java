package dev.qilletni.intellij.library;

import com.intellij.openapi.roots.SyntheticLibrary;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Synthetic library representing a single installed Qilletni library with a name and a single source root.
 */
public final class QilletniSyntheticLibrary extends SyntheticLibrary {
    private final String name;
    private final String presentableName;
    private final List<VirtualFile> sourceRoots;

    public QilletniSyntheticLibrary(String name, String presentableName, List<VirtualFile> sourceRoots) {
        this.name = Objects.requireNonNull(name);
        this.presentableName = presentableName;
        this.sourceRoots = List.copyOf(sourceRoots);
    }

    public String getName() { return name; }

    @Override
    public @NotNull Collection<VirtualFile> getSourceRoots() {
        return sourceRoots;
    }

    public @Nullable String getPresentableNameOrNull() {
        // Not part of SyntheticLibrary API in all platform versions; kept for our own usage.
        return presentableName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QilletniSyntheticLibrary that)) return false;
        return Objects.equals(name, that.name) && Objects.equals(sourceRoots, that.sourceRoots);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, sourceRoots);
    }
}
