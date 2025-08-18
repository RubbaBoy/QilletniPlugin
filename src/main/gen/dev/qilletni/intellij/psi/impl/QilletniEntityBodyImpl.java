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

public class QilletniEntityBodyImpl extends QilletniPsiElementBase implements QilletniEntityBody {

  public QilletniEntityBodyImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull QilletniVisitor visitor) {
    visitor.visitEntityBody(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof QilletniVisitor) accept((QilletniVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public QilletniEntityConstructor getEntityConstructor() {
    return findChildByClass(QilletniEntityConstructor.class);
  }

  @Override
  @NotNull
  public List<QilletniEntityPropertyDeclaration> getEntityPropertyDeclarationList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, QilletniEntityPropertyDeclaration.class);
  }

  @Override
  @NotNull
  public List<QilletniFunctionDef> getFunctionDefList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, QilletniFunctionDef.class);
  }

}
