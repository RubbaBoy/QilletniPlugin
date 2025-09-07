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

public class QilletniWeightsDefineImpl extends QilletniPsiElementBase implements QilletniWeightsDefine {

  public QilletniWeightsDefineImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull QilletniVisitor visitor) {
    visitor.visitWeightsDefine(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof QilletniVisitor) accept((QilletniVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public QilletniFunctionCall getFunctionCall() {
    return PsiTreeUtil.getChildOfType(this, QilletniFunctionCall.class);
  }

  @Override
  @Nullable
  public PsiElement getId() {
    return findChildByType(ID);
  }

  @Override
  @NotNull
  public PsiElement getLeftSbracket() {
    return notNullChild(findChildByType(LEFT_SBRACKET));
  }

  @Override
  @NotNull
  public PsiElement getRightSbracket() {
    return notNullChild(findChildByType(RIGHT_SBRACKET));
  }

  @Override
  @NotNull
  public PsiElement getWeightsKeyword() {
    return notNullChild(findChildByType(WEIGHTS_KEYWORD));
  }

}
