// This is a generated file. Not intended for manual editing.
package dev.qilletni.intellij.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface PrimaryExpr extends PsiElement {

  @Nullable
  AlbumExpr getAlbumExpr();

  @Nullable
  CollectionExpr getCollectionExpr();

  @Nullable
  DoubleExpr getDoubleExpr();

  @Nullable
  EntityInitialize getEntityInitialize();

  @Nullable
  Expr getExpr();

  @Nullable
  FunctionCall getFunctionCall();

  @Nullable
  IntExpr getIntExpr();

  @Nullable
  IsExpr getIsExpr();

  @Nullable
  JavaExpr getJavaExpr();

  @Nullable
  ListExpression getListExpression();

  @Nullable
  SongExpr getSongExpr();

  @Nullable
  StrExpr getStrExpr();

  @Nullable
  WeightsExpr getWeightsExpr();

  @Nullable
  PsiElement getBool();

  @Nullable
  PsiElement getId();

  @Nullable
  PsiElement getLeftParen();

  @Nullable
  PsiElement getRightParen();

}
