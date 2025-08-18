// This is a generated file. Not intended for manual editing.
package dev.qilletni.intellij.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface QilletniCollectionExpr extends PsiElement {

  @Nullable
  QilletniCollectionUrlOrNamePair getCollectionUrlOrNamePair();

  @Nullable
  QilletniListExpression getListExpression();

  @Nullable
  QilletniOrderDefine getOrderDefine();

  @Nullable
  QilletniWeightsDefine getWeightsDefine();

  @Nullable
  PsiElement getCollectionType();

  @Nullable
  PsiElement getLeftParen();

  @Nullable
  PsiElement getRightParen();

  @Nullable
  PsiElement getString();

}
