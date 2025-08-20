package dev.qilletni.intellij.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import dev.qilletni.intellij.resolve.QilletniResolveUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Reference for member name identifiers (properties and methods) that appear in postfix chains (`.name` or `.name(args)`).
 * Resolution is conservative and relies on static receiver type inference.
 */
public final class QilletniMemberReference extends PsiReferenceBase<PsiElement> {
    public QilletniMemberReference(@NotNull PsiElement element, @NotNull TextRange rangeInElement) {
        super(element, rangeInElement);
    }

    @Override
    public @Nullable PsiElement resolve() {
        return QilletniResolveUtil.resolveMemberUsage(getElement());
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName) {
        var el = getElement();
        if (el.getNode() != null) {
            var leaf = new com.intellij.psi.impl.source.tree.LeafPsiElement(dev.qilletni.intellij.psi.QilletniTypes.ID, newElementName);
            el.replace(leaf);
        }
        return el;
    }

    @Override
    public Object @NotNull [] getVariants() {
        return EMPTY_ARRAY;
    }
}
