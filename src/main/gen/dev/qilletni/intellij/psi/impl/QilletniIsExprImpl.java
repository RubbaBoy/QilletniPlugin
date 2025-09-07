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

public class QilletniIsExprImpl extends QilletniPsiElementBase implements QilletniIsExpr {

  public QilletniIsExprImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull QilletniVisitor visitor) {
    visitor.visitIsExpr(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof QilletniVisitor) accept((QilletniVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public PsiElement getAlbumType() {
    return findChildByType(ALBUM_TYPE);
  }

  @Override
  @Nullable
  public PsiElement getAnyType() {
    return findChildByType(ANY_TYPE);
  }

  @Override
  @Nullable
  public PsiElement getBooleanType() {
    return findChildByType(BOOLEAN_TYPE);
  }

  @Override
  @Nullable
  public PsiElement getCollectionType() {
    return findChildByType(COLLECTION_TYPE);
  }

  @Override
  @Nullable
  public PsiElement getDoubleType() {
    return findChildByType(DOUBLE_TYPE);
  }

  @Override
  @Nullable
  public PsiElement getIntType() {
    return findChildByType(INT_TYPE);
  }

  @Override
  @NotNull
  public PsiElement getIsKeyword() {
    return notNullChild(findChildByType(IS_KEYWORD));
  }

  @Override
  @Nullable
  public PsiElement getJavaType() {
    return findChildByType(JAVA_TYPE);
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

  @Override
  @Nullable
  public PsiElement getSongType() {
    return findChildByType(SONG_TYPE);
  }

  @Override
  @Nullable
  public PsiElement getStringType() {
    return findChildByType(STRING_TYPE);
  }

  @Override
  @Nullable
  public PsiElement getWeightsKeyword() {
    return findChildByType(WEIGHTS_KEYWORD);
  }

}
