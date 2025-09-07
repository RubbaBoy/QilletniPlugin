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

public class QilletniPrimaryExprImpl extends QilletniPsiElementBase implements QilletniPrimaryExpr {

  public QilletniPrimaryExprImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull QilletniVisitor visitor) {
    visitor.visitPrimaryExpr(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof QilletniVisitor) accept((QilletniVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public QilletniAlbumExpr getAlbumExpr() {
    return PsiTreeUtil.getChildOfType(this, QilletniAlbumExpr.class);
  }

  @Override
  @Nullable
  public QilletniCollectionExpr getCollectionExpr() {
    return PsiTreeUtil.getChildOfType(this, QilletniCollectionExpr.class);
  }

  @Override
  @Nullable
  public QilletniDoubleExpr getDoubleExpr() {
    return PsiTreeUtil.getChildOfType(this, QilletniDoubleExpr.class);
  }

  @Override
  @Nullable
  public QilletniEntityInitialize getEntityInitialize() {
    return PsiTreeUtil.getChildOfType(this, QilletniEntityInitialize.class);
  }

  @Override
  @Nullable
  public QilletniExpr getExpr() {
    return PsiTreeUtil.getChildOfType(this, QilletniExpr.class);
  }

  @Override
  @Nullable
  public QilletniFunctionCall getFunctionCall() {
    return PsiTreeUtil.getChildOfType(this, QilletniFunctionCall.class);
  }

  @Override
  @Nullable
  public QilletniIntExpr getIntExpr() {
    return PsiTreeUtil.getChildOfType(this, QilletniIntExpr.class);
  }

  @Override
  @Nullable
  public QilletniIsExpr getIsExpr() {
    return PsiTreeUtil.getChildOfType(this, QilletniIsExpr.class);
  }

  @Override
  @Nullable
  public QilletniJavaExpr getJavaExpr() {
    return PsiTreeUtil.getChildOfType(this, QilletniJavaExpr.class);
  }

  @Override
  @Nullable
  public QilletniListExpression getListExpression() {
    return PsiTreeUtil.getChildOfType(this, QilletniListExpression.class);
  }

  @Override
  @Nullable
  public QilletniSongExpr getSongExpr() {
    return PsiTreeUtil.getChildOfType(this, QilletniSongExpr.class);
  }

  @Override
  @Nullable
  public QilletniStrExpr getStrExpr() {
    return PsiTreeUtil.getChildOfType(this, QilletniStrExpr.class);
  }

  @Override
  @Nullable
  public QilletniWeightsExpr getWeightsExpr() {
    return PsiTreeUtil.getChildOfType(this, QilletniWeightsExpr.class);
  }

  @Override
  @Nullable
  public PsiElement getBool() {
    return findChildByType(BOOL);
  }

  @Override
  @Nullable
  public PsiElement getId() {
    return findChildByType(ID);
  }

  @Override
  @Nullable
  public PsiElement getLeftParen() {
    return findChildByType(LEFT_PAREN);
  }

  @Override
  @Nullable
  public PsiElement getRightParen() {
    return findChildByType(RIGHT_PAREN);
  }

}
