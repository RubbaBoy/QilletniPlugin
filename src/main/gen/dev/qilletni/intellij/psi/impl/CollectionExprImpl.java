// This is a generated file. Not intended for manual editing.
package dev.qilletni.intellij.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static dev.qilletni.intellij.psi.QilletniTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import dev.qilletni.intellij.psi.*;

public class CollectionExprImpl extends ASTWrapperPsiElement implements CollectionExpr {

  public CollectionExprImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull Visitor visitor) {
    visitor.visitCollectionExpr(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Visitor) accept((Visitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public CollectionUrlOrNamePair getCollectionUrlOrNamePair() {
    return findChildByClass(CollectionUrlOrNamePair.class);
  }

  @Override
  @Nullable
  public ListExpression getListExpression() {
    return findChildByClass(ListExpression.class);
  }

  @Override
  @Nullable
  public OrderDefine getOrderDefine() {
    return findChildByClass(OrderDefine.class);
  }

  @Override
  @Nullable
  public WeightsDefine getWeightsDefine() {
    return findChildByClass(WeightsDefine.class);
  }

  @Override
  @Nullable
  public PsiElement getCollectionType() {
    return findChildByType(COLLECTION_TYPE);
  }

  @Override
  @Nullable
  public PsiElement getLeftParen() {
    return findChildByType(LEFT_PAREN);
  }

  @Override
  @Nullable
  public PsiElement getRightParen() {
    return findChildByType(RIGHT_PAREN);
  }

  @Override
  @Nullable
  public PsiElement getString() {
    return findChildByType(STRING);
  }

}
