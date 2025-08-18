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

public class QilletniCollectionLimitImpl extends QilletniPsiElementBase implements QilletniCollectionLimit {

  public QilletniCollectionLimitImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull QilletniVisitor visitor) {
    visitor.visitCollectionLimit(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof QilletniVisitor) accept((QilletniVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public QilletniLimitAmount getLimitAmount() {
    return findNotNullChildByClass(QilletniLimitAmount.class);
  }

  @Override
  @NotNull
  public PsiElement getLeftSbracket() {
    return findNotNullChildByType(LEFT_SBRACKET);
  }

  @Override
  @NotNull
  public PsiElement getLimitParam() {
    return findNotNullChildByType(LIMIT_PARAM);
  }

  @Override
  @NotNull
  public PsiElement getRightSbracket() {
    return findNotNullChildByType(RIGHT_SBRACKET);
  }

}
