package dev.qilletni.intellij.resolve;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.psi.util.PsiTreeUtil;
import dev.qilletni.intellij.QilletniFile;
import dev.qilletni.intellij.psi.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Project-wide lookup facade scoped to the current file and its imports.
 * This scans PSI for now; it can be replaced by Stub/FileBased indices later without changing call sites.
 */
public final class QilletniIndexFacade {
    private QilletniIndexFacade() {}

    private static final Key<CachedValue<Map<String, QilletniEntityDef>>> ENTITY_CACHE_KEY =
            Key.create("qilletni.index.entity.byType");
    private static final Key<CachedValue<Map<String, List<QilletniFunctionDef>>>> EXT_METHODS_CACHE_KEY =
            Key.create("qilletni.index.ext.methods");
    private static final Key<CachedValue<List<QilletniEntityDef>>> ENTITY_LIST_CACHE_KEY =
            Key.create("qilletni.index.entities.in.scope");
    private static final Key<CachedValue<List<QilletniFunctionDef>>> TOP_LEVEL_FUNCS_CACHE_KEY =
            Key.create("qilletni.index.top.level.functions");
    private static final Key<CachedValue<Map<String, List<QilletniFunctionDef>>>> EXT_METHODS_BY_TYPE_CACHE_KEY =
            Key.create("qilletni.index.ext.methods.byType");

    public static QilletniEntityDef findEntityByTypeName(Project project, QilletniFile contextFile, String rawTypeName) {
        if (project == null || contextFile == null || rawTypeName == null) return null;

        Map<String, QilletniEntityDef> cache = getOrCreateEntityCache(project, contextFile);
        QilletniEntityDef cached = cache.get(rawTypeName);
        if (cached != null && cached.isValid()) return cached;

        var tn = parseTypeName(rawTypeName);
        List<VirtualFile> files = candidateFilesForType(contextFile, tn);
        for (VirtualFile vf : files) {
            var psi = vf != null ? PsiManager.getInstance(project).findFile(vf) : null;
            if (!(psi instanceof QilletniFile)) continue;
            QilletniEntityDef e = getQilletniEntityDef(rawTypeName, cache, tn, psi);
            if (e != null) return e;
        }

        // Also consider the context file if not included (e.g., no physical VFS)
        if (contextFile.getVirtualFile() == null && tn.qualifier == null) {
            return getQilletniEntityDef(rawTypeName, cache, tn, contextFile);
        }
        // Negative result caching is optional; skip to avoid stale misses.
        return null;
    }

    @Nullable
    private static QilletniEntityDef getQilletniEntityDef(String rawTypeName, Map<String, QilletniEntityDef> cache, TypeName tn, PsiFile psi) {
        for (var e : PsiTreeUtil.findChildrenOfType(psi, QilletniEntityDef.class)) {
            var name = PsiTreeUtil.findChildOfType(e, QilletniEntityName.class);
            if (name != null && name.getText().contentEquals(tn.simple)) {
                cache.put(rawTypeName, e);
                return e;
            }
        }
        return null;
    }

    public static List<QilletniFunctionDef> findExtensionMethods(Project project, QilletniFile contextFile, String rawReceiverType, String methodName) {
        List<QilletniFunctionDef> empty = new ArrayList<>();
        if (project == null || contextFile == null || rawReceiverType == null || methodName == null) return empty;

        Map<String, List<QilletniFunctionDef>> cache = getOrCreateExtMethodsCache(project, contextFile);
        String k = rawReceiverType + "#" + methodName;
        List<QilletniFunctionDef> cached = cache.get(k);
        if (cached != null) {
            // Filter out invalid PSI just in case
            List<QilletniFunctionDef> valid = new ArrayList<>();
            for (var f : cached) if (f != null && f.isValid()) valid.add(f);
            if (!valid.isEmpty()) return valid;
        }

        var tn = parseTypeName(rawReceiverType);
        List<VirtualFile> files = candidateFilesForType(contextFile, tn);
        Set<String> seen = new HashSet<>();
        List<QilletniFunctionDef> result = new ArrayList<>();
        for (VirtualFile vf : files) {
            var psi = vf != null ? PsiManager.getInstance(project).findFile(vf) : null;
            if (!(psi instanceof QilletniFile)) continue;
            for (var def : PsiTreeUtil.findChildrenOfType(psi, QilletniFunctionDef.class)) {
                var onType = PsiTreeUtil.findChildOfType(def, QilletniFunctionOnType.class);
                if (onType == null) return null;
                PsiElement id = PsiTreeUtil.findChildOfType(onType, PsiElement.class);
                if (id == null || id.getNode() == null || id.getNode().getElementType() != QilletniTypes.ID)
                    return null;
                if (!id.getText().contentEquals(tn.simple)) return null;
                var fName = PsiTreeUtil.findChildOfType(def, QilletniFunctionName.class);
                if (fName == null || !fName.getText().contentEquals(methodName)) return null;
                String key = (vf != null ? vf.getPath() : "CTX") + ":" + fName.getText() + ":" + tn.simple;
                if (seen.add(key)) result.add(def);
            }
        }
        // Consider contextFile without VFS
        if (contextFile.getVirtualFile() == null) {
            for (var def : PsiTreeUtil.findChildrenOfType(contextFile, QilletniFunctionDef.class)) {
                var onType = PsiTreeUtil.findChildOfType(def, QilletniFunctionOnType.class);
                if (onType == null) continue;
                PsiElement id = PsiTreeUtil.findChildOfType(onType, PsiElement.class);
                if (id == null || id.getNode() == null || id.getNode().getElementType() != QilletniTypes.ID) continue;
                if (!id.getText().contentEquals(tn.simple)) continue;
                var fName = PsiTreeUtil.findChildOfType(def, QilletniFunctionName.class);
                if (fName == null || !fName.getText().contentEquals(methodName)) continue;
                String key = "CTX:" + fName.getText() + ":" + tn.simple;
                if (seen.add(key)) result.add(def);
            }
        }
        cache.put(k, result);
        return result;
    }

    // List all entities visible from the given file (current file + project-local imports and alias imports)
    public static List<QilletniEntityDef> listEntitiesInScope(Project project, QilletniFile contextFile) {
        List<VirtualFile> files = new ArrayList<>();
        if (contextFile.getVirtualFile() != null) files.add(contextFile.getVirtualFile());
        files.addAll(QilletniAliasResolver.getProjectLocalImports(contextFile));

        return CachedValuesManager.getManager(project).getCachedValue(contextFile, ENTITY_LIST_CACHE_KEY, () -> {
            List<QilletniEntityDef> list = new ArrayList<>();
            for (VirtualFile vf : files) {
                var psi = vf != null ? PsiManager.getInstance(project).findFile(vf) : null;
                if (psi instanceof QilletniFile) {
                    list.addAll(PsiTreeUtil.findChildrenOfType(psi, QilletniEntityDef.class));
                }
            }
            // Include contextFile if no VFS
            if (contextFile.getVirtualFile() == null) {
                list.addAll(PsiTreeUtil.findChildrenOfType(contextFile, QilletniEntityDef.class));
            }
            return CachedValueProvider.Result.create(list, PsiModificationTracker.MODIFICATION_COUNT);
        }, false);
    }

    // List all top-level functions (no receiver/on-type) visible from the given file
    public static List<QilletniFunctionDef> listTopLevelFunctions(Project project, QilletniFile contextFile) {
        List<VirtualFile> files = new ArrayList<>();
        if (contextFile.getVirtualFile() != null) files.add(contextFile.getVirtualFile());
        files.addAll(QilletniAliasResolver.getProjectLocalImports(contextFile));

        return CachedValuesManager.getManager(project).getCachedValue(contextFile, TOP_LEVEL_FUNCS_CACHE_KEY, () -> {
            List<QilletniFunctionDef> list = new ArrayList<>();
            for (VirtualFile vf : files) {
                var psi = vf != null ? PsiManager.getInstance(project).findFile(vf) : null;
                if (!(psi instanceof QilletniFile)) continue;
                collectTopLevelFunctionDefs(list, psi);
            }
            if (contextFile.getVirtualFile() == null) {
                collectTopLevelFunctionDefs(list, contextFile);
            }
            return CachedValueProvider.Result.create(list, PsiModificationTracker.MODIFICATION_COUNT);
        }, false);
    }

    private static void collectTopLevelFunctionDefs(List<QilletniFunctionDef> list, PsiFile psi) {
        for (var def : PsiTreeUtil.findChildrenOfType(psi, QilletniFunctionDef.class)) {
            var onType = PsiTreeUtil.findChildOfType(def, QilletniFunctionOnType.class);
            if (onType != null) continue; // skip extension methods
            if (PsiTreeUtil.getParentOfType(def, QilletniEntityDef.class, false) != null) continue; // skip entity member functions
            list.add(def);
        }
    }

    // List all extension methods for a given receiver type visible from the given file
    public static List<QilletniFunctionDef> listExtensionMethods(Project project, QilletniFile contextFile, String rawReceiverType) {
        if (rawReceiverType == null) return List.of();
        Map<String, List<QilletniFunctionDef>> cache = CachedValuesManager.getManager(project).getCachedValue(contextFile, EXT_METHODS_BY_TYPE_CACHE_KEY, () ->
                CachedValueProvider.Result.create(new HashMap<>(), PsiModificationTracker.MODIFICATION_COUNT), false);
        List<QilletniFunctionDef> cached = cache.get(rawReceiverType);
        if (cached != null) {
            List<QilletniFunctionDef> valid = new ArrayList<>();
            for (var f : cached) if (f != null && f.isValid()) valid.add(f);
            if (!valid.isEmpty()) return valid;
        }

        var tn = parseTypeName(rawReceiverType);
        List<VirtualFile> files = candidateFilesForType(contextFile, tn);
        List<QilletniFunctionDef> list = new ArrayList<>();
        for (VirtualFile vf : files) {
            var psi = vf != null ? PsiManager.getInstance(project).findFile(vf) : null;
            if (!(psi instanceof QilletniFile)) continue;
            collectExtensionMethodDefs(tn, list, psi);
        }
        if (contextFile.getVirtualFile() == null) {
            collectExtensionMethodDefs(tn, list, contextFile);
        }
        cache.put(rawReceiverType, list);
        return list;
    }

    private static void collectExtensionMethodDefs(TypeName tn, List<QilletniFunctionDef> list, PsiFile psi) {
        for (var def : PsiTreeUtil.findChildrenOfType(psi, QilletniFunctionDef.class)) {
            var onType = PsiTreeUtil.findChildOfType(def, QilletniFunctionOnType.class);
            if (onType == null) continue;
            PsiElement id = PsiTreeUtil.findChildOfType(onType, PsiElement.class);
            if (id == null || id.getNode() == null || id.getNode().getElementType() != QilletniTypes.ID) continue;
            if (!id.getText().contentEquals(tn.simple)) continue;
            list.add(def);
        }
    }

    private static List<VirtualFile> candidateFilesForType(QilletniFile contextFile, TypeName tn) {
        List<VirtualFile> res = new ArrayList<>();
        if (tn.qualifier != null) {
            // Qualified name: restrict strictly to files imported under this alias
            res.addAll(QilletniAliasResolver.getFilesForQualifier(contextFile, tn.qualifier));
        } else {
            // Unqualified: current file + project-local imports
            if (contextFile.getVirtualFile() != null) {
                res.add(contextFile.getVirtualFile());
            }
            res.addAll(QilletniAliasResolver.getProjectLocalImports(contextFile));
        }
        return res;
    }

    private static Map<String, QilletniEntityDef> getOrCreateEntityCache(Project project, QilletniFile file) {
        return CachedValuesManager.getManager(project).getCachedValue(file, ENTITY_CACHE_KEY, () ->
                CachedValueProvider.Result.create(new HashMap<>(), PsiModificationTracker.MODIFICATION_COUNT), false);
    }

    private static Map<String, List<QilletniFunctionDef>> getOrCreateExtMethodsCache(Project project, QilletniFile file) {
        return CachedValuesManager.getManager(project).getCachedValue(file, EXT_METHODS_CACHE_KEY, () ->
                CachedValueProvider.Result.create(new HashMap<>(), PsiModificationTracker.MODIFICATION_COUNT), false);
    }

    private static TypeName parseTypeName(String raw) {
        int dot = raw.indexOf('.');
        if (dot > 0 && dot < raw.length() - 1) {
            return new TypeName(raw.substring(0, dot), raw.substring(dot + 1));
        }
        return new TypeName(null, raw);
    }

    private static final class TypeName {
        final String qualifier;
        final String simple;
        TypeName(String qualifier, String simple) {
            this.qualifier = qualifier;
            this.simple = simple;
        }
    }
}
