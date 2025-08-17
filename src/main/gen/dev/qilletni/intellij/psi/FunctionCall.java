// This is a generated file. Not intended for manual editing.
package dev.qilletni.intellij.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface FunctionCall extends PsiElement {

  @Nullable
  ExprList getExprList();

  @NotNull
  PsiElement getId();

  @NotNull
  PsiElement getLeftParen();

  @NotNull
  PsiElement getRightParen();

}
