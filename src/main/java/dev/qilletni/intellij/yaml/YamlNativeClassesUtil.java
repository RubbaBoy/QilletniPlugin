package dev.qilletni.intellij.yaml;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.*;

/**
 * Helpers to detect when a YAML scalar is inside qilletni_info.yml native_classes list.
 */
public final class YamlNativeClassesUtil {
    private YamlNativeClassesUtil() {}

    public static boolean isQilletniInfoYaml(@Nullable PsiFile file) {
        if (file == null) return false;
        var name = file.getName();
        return "qilletni_info.yml".equals(name);
    }

    /** Returns true if the element is a YAMLScalar that is part of the top-level native_classes sequence. */
    public static boolean isInNativeClasses(@NotNull PsiElement element) {
        if (!(element instanceof YAMLScalar)) return false;
        var file = element.getContainingFile();
        if (!isQilletniInfoYaml(file)) return false;

        PsiElement p = element.getParent();
        if (!(p instanceof YAMLSequenceItem)) return false;
        p = p.getParent();
        if (!(p instanceof YAMLSequence seq)) return false;
        p = seq.getParent();
        if (!(p instanceof YAMLKeyValue kv)) return false;
        if (!"native_classes".equals(kv.getKeyText())) return false;
        // Ensure it's at document root mapping (tolerate either direct document or top-level mapping)
        PsiElement rootParent = kv.getParent();
        if (rootParent instanceof YAMLDocument) return true;
        return rootParent instanceof YAMLMapping && rootParent.getParent() instanceof YAMLDocument;
    }
}
