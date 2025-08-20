// This is a generated file. Not intended for manual editing.
package dev.qilletni.intellij.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface QilletniAsmtBase extends PsiElement {

  @Nullable
  QilletniExpr getExpr();

  @Nullable
  QilletniIntExpr getIntExpr();

  @Nullable
  QilletniLhsCore getLhsCore();

  @Nullable
  QilletniLhsMember getLhsMember();

  @Nullable
  QilletniVarDeclaration getVarDeclaration();

  @Nullable
  PsiElement getAssign();

  @Nullable
  PsiElement getDoubleDot();

  @Nullable
  PsiElement getId();

  @Nullable
  PsiElement getLeftSbracket();

  @Nullable
  PsiElement getRightSbracket();

}
