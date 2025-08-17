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

public class IfStmtImpl extends ASTWrapperPsiElement implements IfStmt {

  public IfStmtImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull Visitor visitor) {
    visitor.visitIfStmt(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Visitor) accept((Visitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public Body getBody() {
    return findNotNullChildByClass(Body.class);
  }

  @Override
  @NotNull
  public ElseBody getElseBody() {
    return findNotNullChildByClass(ElseBody.class);
  }

  @Override
  @NotNull
  public ElseifList getElseifList() {
    return findNotNullChildByClass(ElseifList.class);
  }

  @Override
  @NotNull
  public Expr getExpr() {
    return findNotNullChildByClass(Expr.class);
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
