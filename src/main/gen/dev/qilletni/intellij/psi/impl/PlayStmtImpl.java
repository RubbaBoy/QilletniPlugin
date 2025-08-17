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

public class PlayStmtImpl extends ASTWrapperPsiElement implements PlayStmt {

  public PlayStmtImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull Visitor visitor) {
    visitor.visitPlayStmt(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Visitor) accept((Visitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public CollectionLimit getCollectionLimit() {
    return findChildByClass(CollectionLimit.class);
  }

  @Override
  @Nullable
  public Expr getExpr() {
    return findChildByClass(Expr.class);
  }

  @Override
  @Nullable
  public PsiElement getId() {
    return findChildByType(ID);
  }

  @Override
  @Nullable
  public PsiElement getLoopParam() {
    return findChildByType(LOOP_PARAM);
  }

  @Override
  @NotNull
  public PsiElement getPlay() {
    return findNotNullChildByType(PLAY);
  }

}
