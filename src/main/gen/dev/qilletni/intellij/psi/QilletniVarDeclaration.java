// This is a generated file. Not intended for manual editing.
package dev.qilletni.intellij.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface QilletniVarDeclaration extends PsiElement {

  @NotNull
  QilletniExpr getExpr();

  @NotNull
  QilletniVarName getVarName();

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
  PsiElement getDoubleType();

  @Nullable
  PsiElement getId();

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
