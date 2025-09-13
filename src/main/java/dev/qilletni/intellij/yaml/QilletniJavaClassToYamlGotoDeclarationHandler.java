package dev.qilletni.intellij.yaml;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds YAML qilletni_info.yml native_classes entries as go-to targets when Ctrl/Cmd+Clicking a Java class name.
 * Strictly searches project-local qilletni-src roots only.
 */
public class QilletniJavaClassToYamlGotoDeclarationHandler implements GotoDeclarationHandler {
    @Override
    public @Nullable PsiElement[] getGotoDeclarationTargets(@Nullable PsiElement element, int offset, Editor editor) {
        if (!(element instanceof PsiIdentifier id)) return null;
        PsiElement parent = id.getParent();
        if (!(parent instanceof PsiClass cls)) return null;
        if (cls.getNameIdentifier() != id) return null;
        Project project = cls.getProject();
        if (DumbService.isDumb(project)) return null;
        String fqn = cls.getQualifiedName();
        if (fqn == null || fqn.isBlank()) return null;

        List<PsiElement> targets = new ArrayList<>();
        for (VirtualFile srcRoot : findProjectQilletniSrcRoots(project)) {
            VirtualFile yamlVf = srcRoot.findChild("qilletni_info.yml");
            if (yamlVf == null || !yamlVf.isValid()) continue;
            PsiFile psiFile = PsiManager.getInstance(project).findFile(yamlVf);
            if (!(psiFile instanceof YAMLFile yamlFile)) continue;
            collectMatchingNativeClassScalars(yamlFile, fqn, targets);
        }
        return targets.isEmpty() ? null : targets.toArray(PsiElement[]::new);
    }

    private static List<VirtualFile> findProjectQilletniSrcRoots(Project project) {
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

    private static void collectMatchingNativeClassScalars(YAMLFile yamlFile, String fqn, List<PsiElement> out) {
        for (YAMLDocument doc : yamlFile.getDocuments()) {
            YAMLValue topVal = doc.getTopLevelValue();
            if (!(topVal instanceof YAMLMapping mapping)) continue;
            YAMLKeyValue kv = mapping.getKeyValueByKey("native_classes");
            if (kv == null) continue;
            YAMLValue value = kv.getValue();
            if (!(value instanceof YAMLSequence seq)) continue;
            for (YAMLSequenceItem item : seq.getItems()) {
                YAMLValue val = item.getValue();
                if (val instanceof YAMLScalar scalar) {
                    String text = scalar.getTextValue().trim();
                    if (!text.isEmpty() && text.equals(fqn)) {
                        out.add(scalar);
                    }
                }
            }
        }
    }

    @Override
    public @Nullable String getActionText(DataContext context) {
        return null; // default
    }
}
