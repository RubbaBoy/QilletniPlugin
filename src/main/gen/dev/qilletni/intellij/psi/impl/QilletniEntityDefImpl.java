// This is a generated file. Not intended for manual editing.
package dev.qilletni.intellij.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static dev.qilletni.intellij.psi.QilletniTypes.*;
import dev.qilletni.intellij.psi.impl.mixin.QilletniEntityDefMixin;
import dev.qilletni.intellij.psi.*;
import dev.qilletni.intellij.psi.stubs.QilletniEntityDefStub;
import com.intellij.psi.stubs.IStubElementType;

public class QilletniEntityDefImpl extends QilletniEntityDefMixin implements QilletniEntityDef {

  public QilletniEntityDefImpl(@NotNull ASTNode node) {
    super(node);
  }

  public QilletniEntityDefImpl(@NotNull QilletniEntityDefStub stub, @NotNull IStubElementType<?, ?> type) {
    super(stub, type);
  }

  public void accept(@NotNull QilletniVisitor visitor) {
    visitor.visitEntityDef(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof QilletniVisitor) accept((QilletniVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public QilletniEntityBody getEntityBody() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, QilletniEntityBody.class));
  }

  @Override
  @NotNull
  public QilletniEntityName getEntityName() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, QilletniEntityName.class));
  }

  @Override
  @Nullable
  public PsiElement getDocComment() {
    return findChildByType(DOC_COMMENT);
  }

  @Override
  @NotNull
  public PsiElement getEntity() {
    return notNullChild(findChildByType(ENTITY));
  }

  @Override
  @NotNull
  public PsiElement getLeftCbracket() {
    return notNullChild(findChildByType(LEFT_CBRACKET));
  }

  @Override
  @NotNull
  public PsiElement getRightCbracket() {
    return notNullChild(findChildByType(RIGHT_CBRACKET));
  }

}
