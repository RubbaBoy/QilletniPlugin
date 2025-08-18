// This is a generated file. Not intended for manual editing.
package dev.qilletni.intellij.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface QilletniPrimaryExpr extends PsiElement {

  @Nullable
  QilletniAlbumExpr getAlbumExpr();

  @Nullable
  QilletniCollectionExpr getCollectionExpr();

  @Nullable
  QilletniDoubleExpr getDoubleExpr();

  @Nullable
  QilletniEntityInitialize getEntityInitialize();

  @Nullable
  QilletniExpr getExpr();

  @Nullable
  QilletniFunctionCall getFunctionCall();

  @Nullable
  QilletniIntExpr getIntExpr();

  @Nullable
  QilletniIsExpr getIsExpr();

  @Nullable
  QilletniJavaExpr getJavaExpr();

  @Nullable
  QilletniListExpression getListExpression();

  @Nullable
  QilletniSongExpr getSongExpr();

  @Nullable
  QilletniStrExpr getStrExpr();

  @Nullable
  QilletniWeightsExpr getWeightsExpr();

  @Nullable
  PsiElement getBool();

  @Nullable
  PsiElement getId();

  @Nullable
  PsiElement getLeftParen();

  @Nullable
  PsiElement getRightParen();

}
