package dev.qilletni.intellij.library;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.AdditionalLibraryRootsProvider;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.util.indexing.IndexableSetContributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Ensures that Qilletni library source roots (qilletni-src inside .qll archives) are included
 * into the indexing roots regardless of project attachment specifics. This complements
 * QilletniAdditionalLibraryRootsProvider.
 */
public final class QilletniIndexableSetContributor extends IndexableSetContributor {

    @Override
    public @NotNull Set<VirtualFile> getAdditionalRootsToIndex() {
        // Application-level: include all installed library src roots.
        return getRoots();
    }

    @Override
    public @NotNull Set<VirtualFile> getAdditionalProjectRootsToIndex(@NotNull Project project) {
        // Project-level: same set; libraries are application-level but visible to all projects.
        return getRoots();
    }

    private static Set<VirtualFile> getRoots() {
        var mgr = QilletniLibraryManager.getInstance();
        List<VirtualFile> list = new ArrayList<>();
        for (var lib : mgr.getInstalled()) {
            var src = lib.srcRoot();
            if (src != null && src.isValid()) list.add(src);
        }
        return new LinkedHashSet<>(list);
    }
}
