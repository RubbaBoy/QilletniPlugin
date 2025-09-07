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

public class QilletniForExprImpl extends QilletniPsiElementBase implements QilletniForExpr {

  public QilletniForExprImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull QilletniVisitor visitor) {
    visitor.visitForExpr(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof QilletniVisitor) accept((QilletniVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public QilletniExpr getExpr() {
    return PsiTreeUtil.getChildOfType(this, QilletniExpr.class);
  }

  @Override
  @Nullable
  public QilletniForeachRange getForeachRange() {
    return PsiTreeUtil.getChildOfType(this, QilletniForeachRange.class);
  }

  @Override
  @Nullable
  public QilletniRangeExpr getRangeExpr() {
    return PsiTreeUtil.getChildOfType(this, QilletniRangeExpr.class);
  }

}
