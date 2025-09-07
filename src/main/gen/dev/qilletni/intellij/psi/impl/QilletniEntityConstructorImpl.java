// This is a generated file. Not intended for manual editing.
package dev.qilletni.intellij.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static dev.qilletni.intellij.psi.QilletniTypes.*;
import dev.qilletni.intellij.psi.*;

public class QilletniEntityConstructorImpl extends QilletniPsiElementBase implements QilletniEntityConstructor {

  public QilletniEntityConstructorImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull QilletniVisitor visitor) {
    visitor.visitEntityConstructor(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof QilletniVisitor) accept((QilletniVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public QilletniConstructorName getConstructorName() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, QilletniConstructorName.class));
  }

  @Override
  @NotNull
  public QilletniFunctionDefParams getFunctionDefParams() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, QilletniFunctionDefParams.class));
  }

  @Override
  @Nullable
  public PsiElement getDocComment() {
    return findChildByType(DOC_COMMENT);
  }

  @Override
  @NotNull
  public PsiElement getLeftParen() {
    return notNullChild(findChildByType(LEFT_PAREN));
  }

  @Override
  @NotNull
  public PsiElement getRightParen() {
    return notNullChild(findChildByType(RIGHT_PAREN));
  }

}
