// This is a generated file. Not intended for manual editing.
package dev.qilletni.intellij.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static dev.qilletni.intellij.psi.QilletniTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import dev.qilletni.intellij.psi.*;

public class EntityInitializeImpl extends ASTWrapperPsiElement implements EntityInitialize {

  public EntityInitializeImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull Visitor visitor) {
    visitor.visitEntityInitialize(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Visitor) accept((Visitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public ExprList getExprList() {
    return findChildByClass(ExprList.class);
  }

  @Override
  @NotNull
  public PsiElement getId() {
    return findNotNullChildByType(ID);
  }

  @Override
  @NotNull
  public PsiElement getLeftParen() {
    return findNotNullChildByType(LEFT_PAREN);
  }

  @Override
  @NotNull
  public PsiElement getNew() {
    return findNotNullChildByType(NEW);
  }

  @Override
  @NotNull
  public PsiElement getRightParen() {
    return findNotNullChildByType(RIGHT_PAREN);
  }

}
