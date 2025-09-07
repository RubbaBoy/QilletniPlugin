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

public class QilletniAsmtImpl extends QilletniPsiElementBase implements QilletniAsmt {

  public QilletniAsmtImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull QilletniVisitor visitor) {
    visitor.visitAsmt(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof QilletniVisitor) accept((QilletniVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public QilletniAsmtBase getAsmtBase() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, QilletniAsmtBase.class));
  }

  @Override
  @NotNull
  public List<QilletniExpr> getExprList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, QilletniExpr.class);
  }

}
