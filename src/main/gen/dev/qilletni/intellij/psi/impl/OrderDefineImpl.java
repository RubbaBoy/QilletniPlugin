// This is a generated file. Not intended for manual editing.
package dev.qilletni.intellij.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static dev.qilletni.intellij.psi.QilletniTypes.*;
import dev.qilletni.intellij.psi.*;

public class OrderDefineImpl extends QilletniPsiElementBase implements OrderDefine {

  public OrderDefineImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull Visitor visitor) {
    visitor.visitOrderDefine(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Visitor) accept((Visitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public PsiElement getCollectionOrder() {
    return findNotNullChildByType(COLLECTION_ORDER);
  }

  @Override
  @NotNull
  public PsiElement getLeftSbracket() {
    return findNotNullChildByType(LEFT_SBRACKET);
  }

  @Override
  @NotNull
  public PsiElement getOrderParam() {
    return findNotNullChildByType(ORDER_PARAM);
  }

  @Override
  @NotNull
  public PsiElement getRightSbracket() {
    return findNotNullChildByType(RIGHT_SBRACKET);
  }

}
