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

public class QilletniAsmtBaseImpl extends QilletniPsiElementBase implements QilletniAsmtBase {

  public QilletniAsmtBaseImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull QilletniVisitor visitor) {
    visitor.visitAsmtBase(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof QilletniVisitor) accept((QilletniVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public QilletniExpr getExpr() {
    return PsiTreeUtil.getChildOfType(this, QilletniExpr.class);
  }

  @Override
  @Nullable
  public QilletniIntExpr getIntExpr() {
    return PsiTreeUtil.getChildOfType(this, QilletniIntExpr.class);
  }

  @Override
  @Nullable
  public QilletniLhsCore getLhsCore() {
    return PsiTreeUtil.getChildOfType(this, QilletniLhsCore.class);
  }

  @Override
  @Nullable
  public QilletniLhsMember getLhsMember() {
    return PsiTreeUtil.getChildOfType(this, QilletniLhsMember.class);
  }

  @Override
  @Nullable
  public QilletniVarDeclaration getVarDeclaration() {
    return PsiTreeUtil.getChildOfType(this, QilletniVarDeclaration.class);
  }

  @Override
  @Nullable
  public PsiElement getAssign() {
    return findChildByType(ASSIGN);
  }

  @Override
  @Nullable
  public PsiElement getDoubleDot() {
    return findChildByType(DOUBLE_DOT);
  }

  @Override
  @Nullable
  public PsiElement getId() {
    return findChildByType(ID);
  }

  @Override
  @Nullable
  public PsiElement getLeftSbracket() {
    return findChildByType(LEFT_SBRACKET);
  }

  @Override
  @Nullable
  public PsiElement getRightSbracket() {
    return findChildByType(RIGHT_SBRACKET);
  }

}
