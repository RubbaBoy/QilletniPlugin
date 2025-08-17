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

public class EntityBodyImpl extends ASTWrapperPsiElement implements EntityBody {

  public EntityBodyImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull Visitor visitor) {
    visitor.visitEntityBody(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Visitor) accept((Visitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public EntityConstructor getEntityConstructor() {
    return findChildByClass(EntityConstructor.class);
  }

  @Override
  @NotNull
  public List<EntityPropertyDeclaration> getEntityPropertyDeclarationList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, EntityPropertyDeclaration.class);
  }

  @Override
  @NotNull
  public List<FunctionDef> getFunctionDefList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, FunctionDef.class);
  }

}
