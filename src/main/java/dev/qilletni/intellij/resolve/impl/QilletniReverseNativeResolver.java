package dev.qilletni.intellij.resolve.impl;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import dev.qilletni.intellij.QilletniFile;
import dev.qilletni.intellij.library.QilletniYamlInfoUtil;
import dev.qilletni.intellij.psi.QilletniFunctionDef;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Reverse resolver: given a Java PsiMethod, find Qilletni native function definitions in the current project
 * that bind to it. Constraints:
 * - Strictly project sources (never use jars)
 * - Only within qilletni-src roots whose qilletni_info.yml lists the method's containing class FQN
 * - Name-only matching
 */
public final class QilletniReverseNativeResolver {
    private QilletniReverseNativeResolver() {}

    public static @NotNull List<QilletniFunctionDef> resolveQilletniFunctions(@NotNull PsiMethod method) {
        var project = method.getProject();
        if (DumbService.isDumb(project)) return List.of();

        var cls = method.getContainingClass();
        var fqn = cls != null ? cls.getQualifiedName() : null;
        if (fqn == null || fqn.isBlank()) return List.of();
        var name = method.getName();

        List<QilletniFunctionDef> results = new ArrayList<>();
        for (var srcRoot : findProjectQilletniSrcRoots(project)) {
            var natives = QilletniYamlInfoUtil.readNativeClassesFromYaml(project, srcRoot);
            if (!natives.contains(fqn)) continue;
            // Search all .ql files under this root
            collectMatchingFunctionsUnderRoot(project, srcRoot, name, results);
        }
        return List.copyOf(results);
    }

    private static void collectMatchingFunctionsUnderRoot(Project project, VirtualFile root, String name, List<QilletniFunctionDef> out) {
        var psiManager = PsiManager.getInstance(project);
        VfsUtilCore.iterateChildrenRecursively(root, f -> true, fileOrDir -> {
            if (!fileOrDir.isDirectory() && fileOrDir.getName().endsWith(".ql")) {
                var psi = psiManager.findFile(fileOrDir);
                if (psi instanceof QilletniFile) {
                    var defs = PsiTreeUtil.findChildrenOfType(psi, QilletniFunctionDef.class);
                    for (var def : defs) {
                        if (def.getNative() == null) continue;
                        var fnName = def.getFunctionName();
                        var id = fnName == null ? null : fnName.getId();
                        var text = id == null ? null : id.getText();
                        if (name.equals(text)) {
                            out.add(def);
                        }
                    }
                }
            }
            return true;
        });
    }

    private static @NotNull List<VirtualFile> findProjectQilletniSrcRoots(@NotNull Project project) {
        List<VirtualFile> roots = new ArrayList<>();
        for (var module : ModuleManager.getInstance(project).getModules()) {
            var contentRoots = ModuleRootManager.getInstance(module).getContentRoots();
            for (var root : contentRoots) {
                var candidate = root.findChild("qilletni-src");
                if (candidate != null && candidate.isValid()) {
                    var yaml = candidate.findChild("qilletni_info.yml");
                    if (yaml != null && yaml.isValid()) {
                        roots.add(candidate);
                    }
                }
            }
        }
        return roots;
    }
}
