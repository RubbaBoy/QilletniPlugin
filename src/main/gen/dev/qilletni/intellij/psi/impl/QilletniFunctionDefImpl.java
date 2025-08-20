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

public class QilletniFunctionDefImpl extends QilletniPsiElementBase implements QilletniFunctionDef {

  public QilletniFunctionDefImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull QilletniVisitor visitor) {
    visitor.visitFunctionDef(this);
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
  public QilletniFunctionDefParams getFunctionDefParams() {
    return findNotNullChildByClass(QilletniFunctionDefParams.class);
  }

  @Override
  @NotNull
  public QilletniFunctionName getFunctionName() {
    return findNotNullChildByClass(QilletniFunctionName.class);
  }

  @Override
  @Nullable
  public QilletniFunctionOnType getFunctionOnType() {
    return findChildByClass(QilletniFunctionOnType.class);
  }

  @Override
  @Nullable
  public PsiElement getDocComment() {
    return findChildByType(DOC_COMMENT);
  }

  @Override
  @NotNull
  public PsiElement getFunctionDef() {
    return findNotNullChildByType(FUNCTION_DEF);
  }

  @Override
  @Nullable
  public PsiElement getLeftCbracket() {
    return findChildByType(LEFT_CBRACKET);
  }

  @Override
  @NotNull
  public PsiElement getLeftParen() {
    return findNotNullChildByType(LEFT_PAREN);
  }

  @Override
  @Nullable
  public PsiElement getNative() {
    return findChildByType(NATIVE);
  }

  @Override
  @Nullable
  public PsiElement getRightCbracket() {
    return findChildByType(RIGHT_CBRACKET);
  }

  @Override
  @NotNull
  public PsiElement getRightParen() {
    return findNotNullChildByType(RIGHT_PAREN);
  }

  @Override
  @Nullable
  public PsiElement getStatic() {
    return findChildByType(STATIC);
  }

}
