package dev.qilletni.intellij.yaml;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLLanguage;
import org.jetbrains.yaml.psi.YAMLScalar;

public class QilletniYamlReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement(YAMLScalar.class).withLanguage(YAMLLanguage.INSTANCE),
                new QilletniYamlNativeClassReferenceProvider()
        );
    }
}