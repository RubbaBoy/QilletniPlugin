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

public class BodyStmtImpl extends ASTWrapperPsiElement implements BodyStmt {

  public BodyStmtImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull Visitor visitor) {
    visitor.visitBodyStmt(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Visitor) accept((Visitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public Expr getExpr() {
    return findChildByClass(Expr.class);
  }

  @Override
  @Nullable
  public ForStmt getForStmt() {
    return findChildByClass(ForStmt.class);
  }

  @Override
  @Nullable
  public IfStmt getIfStmt() {
    return findChildByClass(IfStmt.class);
  }

  @Override
  @Nullable
  public Stmt getStmt() {
    return findChildByClass(Stmt.class);
  }

}
