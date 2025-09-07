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
        // Alias-qualified names are re-exported transitively.
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
            // Build transitive alias maps: each alias expands to its transitive closure.
            Map<String, List<VirtualFile>> directAliased = QilletniImportUtil.collectAliasedImports(contextFile);
            Map<String, List<VirtualFile>> aliased = new LinkedHashMap<>();
            for (var entry : directAliased.entrySet()) {
                String alias = entry.getKey();
                // Re-export alias-qualified names transitively (recursive). Project-local scope applied.
                List<VirtualFile> files = QilletniImportUtil.getTransitiveFilesForQualifier(contextFile, alias);
                aliased.put(alias, new ArrayList<>(files));
            }
            // Transitive project-local imports (unaliased + aliased contents) form the ambient scope.
            LinkedHashSet<VirtualFile> projectLocal = new LinkedHashSet<>(QilletniImportUtil.getTransitiveProjectLocalImports(contextFile));
            // All imports (project-local + libraries) transitively; kept for completeness/possible future use.
            LinkedHashSet<VirtualFile> all = new LinkedHashSet<>(QilletniImportUtil.getTransitiveAllImportedFiles(contextFile));
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
