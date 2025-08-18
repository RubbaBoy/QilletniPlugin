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

public class AlbumUrlOrNamePairImpl extends QilletniPsiElementBase implements AlbumUrlOrNamePair {

  public AlbumUrlOrNamePairImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull Visitor visitor) {
    visitor.visitAlbumUrlOrNamePair(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Visitor) accept((Visitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public PsiElement getAlbumType() {
    return findNotNullChildByType(ALBUM_TYPE);
  }

  @Override
  @NotNull
  public PsiElement getBy() {
    return findNotNullChildByType(BY);
  }

}
