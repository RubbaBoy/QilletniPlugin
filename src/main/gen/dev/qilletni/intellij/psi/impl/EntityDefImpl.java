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

public class EntityDefImpl extends QilletniPsiElementBase implements EntityDef {

  public EntityDefImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull Visitor visitor) {
    visitor.visitEntityDef(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Visitor) accept((Visitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public EntityBody getEntityBody() {
    return findNotNullChildByClass(EntityBody.class);
  }

  @Override
  @Nullable
  public PsiElement getDocComment() {
    return findChildByType(DOC_COMMENT);
  }

  @Override
  @NotNull
  public PsiElement getEntity() {
    return findNotNullChildByType(ENTITY);
  }

  @Override
  @NotNull
  public PsiElement getId() {
    return findNotNullChildByType(ID);
  }

  @Override
  @NotNull
  public PsiElement getLeftCbracket() {
    return findNotNullChildByType(LEFT_CBRACKET);
  }

  @Override
  @NotNull
  public PsiElement getRightCbracket() {
    return findNotNullChildByType(RIGHT_CBRACKET);
  }

}
