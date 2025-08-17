// This is a generated file. Not intended for manual editing.
package dev.qilletni.intellij.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface UnaryExpr extends PsiElement {

  @Nullable
  Expr getExpr();

  @Nullable
  ImmutablePostfixExprSuffix getImmutablePostfixExprSuffix();

  @Nullable
  PostfixExpr getPostfixExpr();

  @Nullable
  UnaryExpr getUnaryExpr();

  @Nullable
  PsiElement getDecrement();

  @Nullable
  PsiElement getIncrement();

  @Nullable
  PsiElement getLeftSbracket();

  @Nullable
  PsiElement getNot();

  @Nullable
  PsiElement getRightSbracket();

}
