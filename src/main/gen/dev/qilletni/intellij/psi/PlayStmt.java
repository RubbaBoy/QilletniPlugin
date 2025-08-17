// This is a generated file. Not intended for manual editing.
package dev.qilletni.intellij.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface PlayStmt extends PsiElement {

  @Nullable
  CollectionLimit getCollectionLimit();

  @Nullable
  Expr getExpr();

  @Nullable
  PsiElement getId();

  @Nullable
  PsiElement getLoopParam();

  @NotNull
  PsiElement getPlay();

}
