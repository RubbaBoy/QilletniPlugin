// This is a generated file. Not intended for manual editing.
package dev.qilletni.intellij.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface QilletniRelationalExpr extends PsiElement {

  @NotNull
  List<QilletniAdditiveExpr> getAdditiveExprList();

  @Nullable
  PsiElement getRelOp();

}
