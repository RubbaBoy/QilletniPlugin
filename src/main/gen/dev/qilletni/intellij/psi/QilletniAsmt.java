// This is a generated file. Not intended for manual editing.
package dev.qilletni.intellij.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface QilletniAsmt extends PsiElement {

  @Nullable
  QilletniAsmt getAsmt();

  @NotNull
  List<QilletniExpr> getExprList();

  @Nullable
  QilletniIntExpr getIntExpr();

  @Nullable
  PsiElement getAlbumType();

  @Nullable
  PsiElement getAnyType();

  @NotNull
  PsiElement getAssign();

  @Nullable
  PsiElement getBooleanType();

  @Nullable
  PsiElement getCollectionType();

  @Nullable
  PsiElement getDot();

  @Nullable
  PsiElement getDoubleDot();

  @Nullable
  PsiElement getDoubleType();

  @Nullable
  PsiElement getIntType();

  @Nullable
  PsiElement getJavaType();

  @Nullable
  PsiElement getLeftSbracket();

  @Nullable
  PsiElement getRightSbracket();

  @Nullable
  PsiElement getSongType();

  @Nullable
  PsiElement getStringType();

  @Nullable
  PsiElement getWeightsKeyword();

}
