package dev.qilletni.intellij.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import dev.qilletni.intellij.resolve.QilletniResolveUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Reference for variable usages (bare IDs and LHS non-declaration assignments).
 */
public final class QilletniVariableReference extends PsiReferenceBase<PsiElement> {
    public QilletniVariableReference(@NotNull PsiElement element, @NotNull TextRange rangeInElement) {
        super(element, rangeInElement);
    }

    @Override
    public @Nullable PsiElement resolve() {
        return QilletniResolveUtil.resolveVariableUsage(getElement());
    }

    @Override
    public Object @NotNull [] getVariants() {
        return EMPTY_ARRAY;
    }
}
