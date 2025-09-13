package dev.qilletni.intellij.yaml;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLScalar;

public class QilletniYamlNativeClassReferenceProvider extends PsiReferenceProvider {
    @Override
    public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        if (element instanceof YAMLScalar scalar && YamlNativeClassesUtil.isInNativeClasses(scalar)) {
            return new PsiReference[]{ new QilletniYamlNativeClassReference(scalar) };
        }
        return PsiReference.EMPTY_ARRAY;
    }
}