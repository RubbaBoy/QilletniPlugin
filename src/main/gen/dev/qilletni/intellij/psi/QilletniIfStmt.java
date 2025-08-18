// This is a generated file. Not intended for manual editing.
package dev.qilletni.intellij.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface QilletniIfStmt extends PsiElement {

  @NotNull
  QilletniBody getBody();

  @NotNull
  QilletniElseBody getElseBody();

  @NotNull
  QilletniElseifList getElseifList();

  @NotNull
  QilletniExpr getExpr();

  @NotNull
  PsiElement getIfKeyword();

  @NotNull
  PsiElement getLeftCbracket();

  @NotNull
  PsiElement getLeftParen();

  @NotNull
  PsiElement getRightCbracket();

  @NotNull
  PsiElement getRightParen();

}
