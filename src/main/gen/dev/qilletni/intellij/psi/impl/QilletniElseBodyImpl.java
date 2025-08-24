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

public class QilletniElseBodyImpl extends QilletniPsiElementBase implements QilletniElseBody {

  public QilletniElseBodyImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull QilletniVisitor visitor) {
    visitor.visitElseBody(this);
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
  @NotNull
  public PsiElement getElseKeyword() {
    return findNotNullChildByType(ELSE_KEYWORD);
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
