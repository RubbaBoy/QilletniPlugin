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

public class QilletniStmtImpl extends QilletniPsiElementBase implements QilletniStmt {

  public QilletniStmtImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull QilletniVisitor visitor) {
    visitor.visitStmt(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof QilletniVisitor) accept((QilletniVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public QilletniAsmt getAsmt() {
    return PsiTreeUtil.getChildOfType(this, QilletniAsmt.class);
  }

  @Override
  @Nullable
  public QilletniEntityDef getEntityDef() {
    return PsiTreeUtil.getChildOfType(this, QilletniEntityDef.class);
  }

  @Override
  @Nullable
  public QilletniPlayStmt getPlayStmt() {
    return PsiTreeUtil.getChildOfType(this, QilletniPlayStmt.class);
  }

  @Override
  @Nullable
  public QilletniProviderStmt getProviderStmt() {
    return PsiTreeUtil.getChildOfType(this, QilletniProviderStmt.class);
  }

}
