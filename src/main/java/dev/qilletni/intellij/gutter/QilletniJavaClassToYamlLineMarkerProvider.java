package dev.qilletni.intellij.gutter;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.PsiNavigateUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.*;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Shows a gutter on Java classes that are referenced in project-local qilletni_info.yml native_classes.
 */
public class QilletniJavaClassToYamlLineMarkerProvider implements LineMarkerProvider {
    @Override
    public @Nullable LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        if (!(element instanceof PsiIdentifier id)) return null;
        if (!(id.getParent() instanceof PsiClass cls)) return null;
        if (cls.getNameIdentifier() != id) return null;
        var project = cls.getProject();
        if (DumbService.isDumb(project)) return null;
        var fqn = cls.getQualifiedName();
        if (fqn == null || fqn.isBlank()) return null;

        List<YAMLScalar> targets = findYamlUsages(project, fqn);
        if (targets.isEmpty()) return null;

        Icon icon = AllIcons.Gutter.ImplementingMethod;
        String tooltip = buildTooltip(targets);
        return new LineMarkerInfo<>(
                id,
                id.getTextRange(),
                icon,
                psi -> tooltip,
                (e, elt) -> openChooser(e, targets),
                GutterIconRenderer.Alignment.CENTER,
                () -> "Listed in qilletni_info.yml native_classes"
        );
    }

    private static String buildTooltip(List<YAMLScalar> targets) {
        var sb = new StringBuilder("qilletni_info.yml native_classes: ");
        int shown = 0;
        for (var s : targets) {
            if (shown > 0) sb.append(", ");
            var file = s.getContainingFile();
            var fileName = file != null ? file.getName() : "qilletni_info.yml";
            sb.append(fileName);
            shown++;
            if (shown == 3) break;
        }
        if (targets.size() > 3) sb.append(", …");
        return sb.toString();
    }

    private static void openChooser(MouseEvent e, List<YAMLScalar> targets) {
        if (targets.size() == 1) {
            PsiNavigateUtil.navigate(targets.getFirst());
            return;
        }
        NavigationUtil.getPsiElementPopup(targets.toArray(PsiElement.EMPTY_ARRAY), "qilletni_info.yml usages").show(new RelativePoint(e));
    }

    private static List<YAMLScalar> findYamlUsages(Project project, String fqn) {
        List<YAMLScalar> out = new ArrayList<>();
        var moduleManager = com.intellij.openapi.module.ModuleManager.getInstance(project);
        for (var module : moduleManager.getModules()) {
            var roots = com.intellij.openapi.roots.ModuleRootManager.getInstance(module).getContentRoots();
            for (var root : roots) {
                var src = root.findChild("qilletni-src");
                if (src == null || !src.isValid()) continue;
                var yamlVf = src.findChild("qilletni_info.yml");
                if (yamlVf == null || !yamlVf.isValid()) continue;
                var psi = PsiManager.getInstance(project).findFile(yamlVf);
                if (!(psi instanceof YAMLFile yamlFile)) continue;
                collectMatchingNativeClassScalars(yamlFile, fqn, out);
            }
        }
        return out;
    }

    private static void collectMatchingNativeClassScalars(YAMLFile yamlFile, String fqn, List<YAMLScalar> out) {
        for (YAMLDocument doc : yamlFile.getDocuments()) {
            YAMLValue top = doc.getTopLevelValue();
            if (!(top instanceof YAMLMapping mapping)) continue;
            YAMLKeyValue kv = mapping.getKeyValueByKey("native_classes");
            if (kv == null) continue;
            YAMLValue value = kv.getValue();
            if (!(value instanceof YAMLSequence seq)) continue;
            for (YAMLSequenceItem item : seq.getItems()) {
                var v = item.getValue();
                if (v instanceof YAMLScalar scalar) {
                    var text = scalar.getTextValue().trim();
                    if (!text.isEmpty() && text.equals(fqn)) out.add(scalar);
                }
            }
        }
    }
}
