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

public class QilletniWeightAmountImpl extends QilletniPsiElementBase implements QilletniWeightAmount {

  public QilletniWeightAmountImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull QilletniVisitor visitor) {
    visitor.visitWeightAmount(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof QilletniVisitor) accept((QilletniVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public PsiElement getInt() {
    return notNullChild(findChildByType(INT));
  }

  @Override
  @NotNull
  public PsiElement getWeightUnit() {
    return notNullChild(findChildByType(WEIGHT_UNIT));
  }

}
