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

public class ProgImpl extends QilletniPsiElementBase implements Prog {

  public ProgImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull Visitor visitor) {
    visitor.visitProg(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Visitor) accept((Visitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<ImportFile> getImportFileList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ImportFile.class);
  }

  @Override
  @NotNull
  public List<Running> getRunningList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, Running.class);
  }

}
