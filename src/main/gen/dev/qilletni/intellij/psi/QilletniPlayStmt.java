// This is a generated file. Not intended for manual editing.
package dev.qilletni.intellij.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface QilletniPlayStmt extends PsiElement {

  @Nullable
  QilletniCollectionLimit getCollectionLimit();

  @Nullable
  QilletniExpr getExpr();

  @Nullable
  PsiElement getId();

  @Nullable
  PsiElement getLoopParam();

  @NotNull
  PsiElement getPlay();

}
