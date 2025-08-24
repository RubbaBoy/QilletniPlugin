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

public class QilletniIfStmtImpl extends QilletniPsiElementBase implements QilletniIfStmt {

  public QilletniIfStmtImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull QilletniVisitor visitor) {
    visitor.visitIfStmt(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof QilletniVisitor) accept((QilletniVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public QilletniBody getBody() {
    return findNotNullChildByClass(QilletniBody.class);
  }

  @Override
  @Nullable
  public QilletniElseBody getElseBody() {
    return findChildByClass(QilletniElseBody.class);
  }

  @Override
  @NotNull
  public List<QilletniElseifList> getElseifListList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, QilletniElseifList.class);
  }

  @Override
  @NotNull
  public QilletniExpr getExpr() {
    return findNotNullChildByClass(QilletniExpr.class);
  }

  @Override
  @NotNull
  public PsiElement getIfKeyword() {
    return findNotNullChildByType(IF_KEYWORD);
  }

  @Override
  @NotNull
  public PsiElement getLeftCbracket() {
    return findNotNullChildByType(LEFT_CBRACKET);
  }

  @Override
  @NotNull
  public PsiElement getLeftParen() {
    return findNotNullChildByType(LEFT_PAREN);
  }

  @Override
  @NotNull
  public PsiElement getRightCbracket() {
    return findNotNullChildByType(RIGHT_CBRACKET);
  }

  @Override
  @NotNull
  public PsiElement getRightParen() {
    return findNotNullChildByType(RIGHT_PAREN);
  }

}
