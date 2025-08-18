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

public class QilletniUnaryExprImpl extends QilletniPsiElementBase implements QilletniUnaryExpr {

  public QilletniUnaryExprImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull QilletniVisitor visitor) {
    visitor.visitUnaryExpr(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof QilletniVisitor) accept((QilletniVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public QilletniExpr getExpr() {
    return findChildByClass(QilletniExpr.class);
  }

  @Override
  @Nullable
  public QilletniImmutablePostfixExprSuffix getImmutablePostfixExprSuffix() {
    return findChildByClass(QilletniImmutablePostfixExprSuffix.class);
  }

  @Override
  @Nullable
  public QilletniPostfixExpr getPostfixExpr() {
    return findChildByClass(QilletniPostfixExpr.class);
  }

  @Override
  @Nullable
  public QilletniUnaryExpr getUnaryExpr() {
    return findChildByClass(QilletniUnaryExpr.class);
  }

  @Override
  @Nullable
  public PsiElement getDecrement() {
    return findChildByType(DECREMENT);
  }

  @Override
  @Nullable
  public PsiElement getIncrement() {
    return findChildByType(INCREMENT);
  }

  @Override
  @Nullable
  public PsiElement getLeftSbracket() {
    return findChildByType(LEFT_SBRACKET);
  }

  @Override
  @Nullable
  public PsiElement getNot() {
    return findChildByType(NOT);
  }

  @Override
  @Nullable
  public PsiElement getRightSbracket() {
    return findChildByType(RIGHT_SBRACKET);
  }

}
