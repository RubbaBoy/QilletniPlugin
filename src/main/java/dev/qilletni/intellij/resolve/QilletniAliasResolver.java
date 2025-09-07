package dev.qilletni.intellij.resolve;

import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.openapi.roots.ProjectRootManager;
import dev.qilletni.intellij.QilletniFile;

import java.util.*;

/**
 * Alias-aware resolution facade for imported files.
 * Uses CachedValues per file to avoid rescanning PSI on every query.
 * External dependency resolution can be added later without changing callers.
 */
public final class QilletniAliasResolver {
    private QilletniAliasResolver() {}

    private static final com.intellij.openapi.util.SimpleModificationTracker ROOTS_TRACKER = new com.intellij.openapi.util.SimpleModificationTracker();
    public static void onRootsChanged() { ROOTS_TRACKER.incModificationCount(); }
    public static com.intellij.openapi.util.ModificationTracker rootsTracker() { return ROOTS_TRACKER; }

    private static final Key<CachedValue<AliasData>> KEY = Key.create("qilletni.alias.data");

    public static Map<String, List<VirtualFile>> getAliasToFiles(QilletniFile contextFile) {
        AliasData data = getData(contextFile);
        // return defensive copy
        Map<String, List<VirtualFile>> out = new LinkedHashMap<>();
        data.aliasToFiles.forEach((k, v) -> out.put(k, List.copyOf(v)));
        return out;
    }

    public static List<VirtualFile> getFilesForQualifier(QilletniFile contextFile, String qualifier) {
        AliasData data = getData(contextFile);
        return List.copyOf(data.aliasToFiles.getOrDefault(qualifier, List.of()));
    }

    public static List<VirtualFile> getProjectLocalImports(QilletniFile contextFile) {
        AliasData data = getData(contextFile);
        return List.copyOf(data.projectLocalImports);
    }

    public static List<VirtualFile> getAllImportedFiles(QilletniFile contextFile) {
        AliasData data = getData(contextFile);
        return List.copyOf(data.allImports);
    }

    private static AliasData getData(QilletniFile contextFile) {
        return CachedValuesManager.getCachedValue(contextFile, KEY, () -> {
            Map<String, List<VirtualFile>> aliased = QilletniImportUtil.collectAliasedImports(contextFile);
            // ensure lists are mutable internally
            aliased.replaceAll((k, v) -> v == null ? new ArrayList<>() : new ArrayList<>(v));
            LinkedHashSet<VirtualFile> projectLocal = new LinkedHashSet<>(QilletniImportUtil.getProjectLocalImports(contextFile));
            // Merge aliased imports into project-local imports set as well
            for (List<VirtualFile> vfs : aliased.values()) projectLocal.addAll(vfs);
            LinkedHashSet<VirtualFile> all = new LinkedHashSet<>(projectLocal);
            AliasData value = new AliasData(aliased, new ArrayList<>(projectLocal), new ArrayList<>(all));
            return CachedValueProvider.Result.create(value, PsiModificationTracker.MODIFICATION_COUNT, ROOTS_TRACKER);
        });
    }

    private static final class AliasData {
        final Map<String, List<VirtualFile>> aliasToFiles;
        final List<VirtualFile> projectLocalImports;
        final List<VirtualFile> allImports;
        AliasData(Map<String, List<VirtualFile>> aliasToFiles, List<VirtualFile> projectLocalImports, List<VirtualFile> allImports) {
            this.aliasToFiles = aliasToFiles;
            this.projectLocalImports = projectLocalImports;
            this.allImports = allImports;
        }
    }
}
