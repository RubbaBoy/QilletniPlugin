package dev.qilletni.intellij.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import org.jetbrains.annotations.NotNull;

/**
 * Base mixin for all named Qilletni PSI elements declared via BNF 'methods' attribute.
 * Methods delegate to QilletniPsiImplUtil to keep logic centralized.
 */
public class QilletniNamedElementImpl extends QilletniPsiElementBase implements PsiNameIdentifierOwner {
    public QilletniNamedElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public String getName() {
        return QilletniPsiImplUtil.getName(this);
    }

    @Override
    public PsiElement getNameIdentifier() {
        return QilletniPsiImplUtil.getNameIdentifier(this);
    }

    @Override
    public PsiElement setName(@NotNull String name) {
        return QilletniPsiImplUtil.setName(this, name);
    }
}
