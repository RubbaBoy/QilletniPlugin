// This is a generated file. Not intended for manual editing.
package dev.qilletni.intellij.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface QilletniImmutablePostfixExprSuffix extends PsiElement {

  @Nullable
  QilletniExpr getExpr();

  @Nullable
  PsiElement getDecrement();

  @Nullable
  PsiElement getIncrement();

  @Nullable
  PsiElement getMinusEquals();

  @Nullable
  PsiElement getPlusEquals();

}
