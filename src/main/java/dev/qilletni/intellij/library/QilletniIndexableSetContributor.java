package dev.qilletni.intellij.library;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.indexing.IndexableSetContributor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Ensures that Qilletni library roots (qilletni-src and native.jar inside .qll archives) are included
 * into the indexing roots regardless of project attachment specifics. This complements
 * QilletniAdditionalLibraryRootsProvider.
 */
public final class QilletniIndexableSetContributor extends IndexableSetContributor {

    @Override
    public @NotNull Set<VirtualFile> getAdditionalRootsToIndex() {
        return getRoots();
    }

    @Override
    public @NotNull Set<VirtualFile> getAdditionalProjectRootsToIndex(@NotNull Project project) {
        return getRoots();
    }

    private static Set<VirtualFile> getRoots() {
        var mgr = QilletniLibraryManager.getInstance();
        List<VirtualFile> list = new ArrayList<>();
        for (var lib : mgr.getInstalled()) {
            var src = lib.srcRoot();
            if (src != null && src.isValid()) list.add(src);
            var jar = lib.nativeJarRoot();
            if (jar != null && jar.isValid()) list.add(jar);
        }
        return new LinkedHashSet<>(list);
    }
}
