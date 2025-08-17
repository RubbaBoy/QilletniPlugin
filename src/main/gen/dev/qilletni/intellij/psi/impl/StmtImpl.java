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

public class StmtImpl extends ASTWrapperPsiElement implements Stmt {

  public StmtImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull Visitor visitor) {
    visitor.visitStmt(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Visitor) accept((Visitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public Asmt getAsmt() {
    return findChildByClass(Asmt.class);
  }

  @Override
  @Nullable
  public EntityDef getEntityDef() {
    return findChildByClass(EntityDef.class);
  }

  @Override
  @Nullable
  public PlayStmt getPlayStmt() {
    return findChildByClass(PlayStmt.class);
  }

  @Override
  @Nullable
  public ProviderStmt getProviderStmt() {
    return findChildByClass(ProviderStmt.class);
  }

}
