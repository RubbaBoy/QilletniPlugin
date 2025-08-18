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

public class QilletniProviderStmtImpl extends QilletniPsiElementBase implements QilletniProviderStmt {

  public QilletniProviderStmtImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull QilletniVisitor visitor) {
    visitor.visitProviderStmt(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof QilletniVisitor) accept((QilletniVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public QilletniBody getBody() {
    return findChildByClass(QilletniBody.class);
  }

  @Override
  @NotNull
  public QilletniStrExpr getStrExpr() {
    return findNotNullChildByClass(QilletniStrExpr.class);
  }

  @Override
  @Nullable
  public PsiElement getLeftCbracket() {
    return findChildByType(LEFT_CBRACKET);
  }

  @Override
  @NotNull
  public PsiElement getProvider() {
    return findNotNullChildByType(PROVIDER);
  }

  @Override
  @Nullable
  public PsiElement getRightCbracket() {
    return findChildByType(RIGHT_CBRACKET);
  }

}
