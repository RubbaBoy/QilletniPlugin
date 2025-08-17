// This is a generated file. Not intended for manual editing.
package dev.qilletni.intellij.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface CollectionExpr extends PsiElement {

  @Nullable
  CollectionUrlOrNamePair getCollectionUrlOrNamePair();

  @Nullable
  ListExpression getListExpression();

  @Nullable
  OrderDefine getOrderDefine();

  @Nullable
  WeightsDefine getWeightsDefine();

  @Nullable
  PsiElement getCollectionType();

  @Nullable
  PsiElement getLeftParen();

  @Nullable
  PsiElement getRightParen();

  @Nullable
  PsiElement getString();

}
