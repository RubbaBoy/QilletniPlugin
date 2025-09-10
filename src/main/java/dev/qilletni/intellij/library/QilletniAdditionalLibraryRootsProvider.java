package dev.qilletni.intellij.library;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.AdditionalLibraryRootsProvider;
import com.intellij.openapi.roots.SyntheticLibrary;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Provides SyntheticLibraries for each installed Qilletni library, exposing their qilletni-src as source roots.
 */
public final class QilletniAdditionalLibraryRootsProvider extends AdditionalLibraryRootsProvider {
    @Override
    public @NotNull Collection<SyntheticLibrary> getAdditionalProjectLibraries(@NotNull Project project) {
        var libs = QilletniLibraryManager.getInstance().getInstalled();
        List<SyntheticLibrary> result = new ArrayList<>();
        for (var l : libs) {
            var src = l.srcRoot();
            if (src == null) continue;
            result.add(new QilletniSyntheticLibrary(l.name(), l.name() + " " + (l.version() != null ? l.version().getVersionString() : ""), List.of(src)));
        }
        return result;
    }

}
