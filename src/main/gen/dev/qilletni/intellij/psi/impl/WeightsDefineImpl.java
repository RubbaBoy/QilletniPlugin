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

public class WeightsDefineImpl extends QilletniPsiElementBase implements WeightsDefine {

  public WeightsDefineImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull Visitor visitor) {
    visitor.visitWeightsDefine(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Visitor) accept((Visitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public FunctionCall getFunctionCall() {
    return findChildByClass(FunctionCall.class);
  }

  @Override
  @Nullable
  public PsiElement getId() {
    return findChildByType(ID);
  }

  @Override
  @NotNull
  public PsiElement getLeftSbracket() {
    return findNotNullChildByType(LEFT_SBRACKET);
  }

  @Override
  @NotNull
  public PsiElement getRightSbracket() {
    return findNotNullChildByType(RIGHT_SBRACKET);
  }

  @Override
  @NotNull
  public PsiElement getWeightsKeyword() {
    return findNotNullChildByType(WEIGHTS_KEYWORD);
  }

}
