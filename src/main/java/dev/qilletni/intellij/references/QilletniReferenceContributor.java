package dev.qilletni.intellij.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import dev.qilletni.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

/**
 * Registers PsiReference providers for Qilletni, modeled after CLIPSReferenceContributor.
 * We attach providers only to usage contexts via precise ElementPatterns.
 */
public final class QilletniReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        // Variable usages: ID tokens that are not declarations, not member name segments, and not function call heads
        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement(QilletniTypes.ID)
                        .andNot(PlatformPatterns.psiElement().withParent(QilletniFunctionName.class))
                        .andNot(PlatformPatterns.psiElement().withParent(QilletniEntityName.class))
                        .andNot(PlatformPatterns.psiElement().withParent(QilletniPropertyName.class))
                        .andNot(PlatformPatterns.psiElement().withParent(QilletniConstructorName.class))
                        .andNot(PlatformPatterns.psiElement().withParent(QilletniVarName.class))
                        .andNot(PlatformPatterns.psiElement().withParent(QilletniParamName.class))
                        .andNot(PlatformPatterns.psiElement().withParent(QilletniFunctionCall.class))
                        .andNot(PlatformPatterns.psiElement().withParent(QilletniPostfixSuffix.class)),
                new PsiReferenceProvider() {
                    @Override
                    public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                        System.out.println("Here we are in QilletniReferenceContributor for " + element.getText());
                        if (element.getNode() == null || element.getNode().getElementType() != QilletniTypes.ID) return PsiReference.EMPTY_ARRAY;
                        System.out.println("\t^ doing it");
                        // Exclude declaration-name nodes by class name suffix heuristic consistent with generated PSI
                        var parent = element.getParent();
                        if (parent != null) {
                            var cls = parent.getClass().getSimpleName();
                            if (cls.endsWith("FunctionName") || cls.endsWith("EntityName") ||
                                    cls.endsWith("PropertyName") || cls.endsWith("ConstructorName") ||
                                    cls.endsWith("VarName") || cls.endsWith("ParamName")) {
                                return PsiReference.EMPTY_ARRAY;
                            }
                        }
                        System.out.println("\t^ eally");
                        var range = TextRange.from(0, element.getTextLength());
                        return new PsiReference[]{ new QilletniVariableReference(element, range) };
                    }
                }
        );

        // Function call heads WITHOUT receiver: ID under FunctionCall whose parent is NOT a PostfixSuffix (i.e., bare call)
        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement(QilletniTypes.ID)
                        .withParent(QilletniFunctionCall.class)
                        .andNot(PlatformPatterns.psiElement().withSuperParent(2, QilletniPostfixSuffix.class)),
                new PsiReferenceProvider() {
                    @Override
                    public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                        var range = TextRange.from(0, element.getTextLength());
                        return new PsiReference[]{ new QilletniFunctionReference(element, range) };
                    }
                }
        );

        // Member name segments (properties or methods):
        // 1) ID that is directly under PostfixSuffix (i.e., `. ID`)
        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement(QilletniTypes.ID)
                        .withParent(QilletniPostfixSuffix.class),
                new MemberReferenceProvider()
        );

        // 2) Function call that appears as a postfix suffix (i.e., `. name(args)`): ID under FunctionCall whose super parent is PostfixSuffix
        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement(QilletniTypes.ID)
                        .withParent(QilletniFunctionCall.class)
                        .withSuperParent(2, QilletniPostfixSuffix.class),
                new MemberReferenceProvider()
        );
    }

    private static final class MemberReferenceProvider extends PsiReferenceProvider {
        @Override
        public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
            var range = TextRange.from(0, element.getTextLength());
            return new PsiReference[]{ new QilletniMemberReference(element, range) };
        }
    }
}
