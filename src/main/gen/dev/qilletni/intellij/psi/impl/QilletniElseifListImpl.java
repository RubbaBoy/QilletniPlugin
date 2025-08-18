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

public class QilletniElseifListImpl extends QilletniPsiElementBase implements QilletniElseifList {

  public QilletniElseifListImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull QilletniVisitor visitor) {
    visitor.visitElseifList(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof QilletniVisitor) accept((QilletniVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<QilletniBody> getBodyList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, QilletniBody.class);
  }

  @Override
  @NotNull
  public List<QilletniExpr> getExprList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, QilletniExpr.class);
  }

}
