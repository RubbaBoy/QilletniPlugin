// This is a generated file. Not intended for manual editing.
package dev.qilletni.intellij.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface QilletniAsmtBase extends PsiElement {

  @NotNull
  QilletniExpr getExpr();

  @Nullable
  QilletniIntExpr getIntExpr();

  @Nullable
  QilletniLhsCore getLhsCore();

  @Nullable
  QilletniLhsMember getLhsMember();

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
