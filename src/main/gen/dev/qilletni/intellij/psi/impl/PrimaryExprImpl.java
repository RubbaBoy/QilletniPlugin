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

public class PrimaryExprImpl extends QilletniPsiElementBase implements PrimaryExpr {

  public PrimaryExprImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull Visitor visitor) {
    visitor.visitPrimaryExpr(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Visitor) accept((Visitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public AlbumExpr getAlbumExpr() {
    return findChildByClass(AlbumExpr.class);
  }

  @Override
  @Nullable
  public CollectionExpr getCollectionExpr() {
    return findChildByClass(CollectionExpr.class);
  }

  @Override
  @Nullable
  public DoubleExpr getDoubleExpr() {
    return findChildByClass(DoubleExpr.class);
  }

  @Override
  @Nullable
  public EntityInitialize getEntityInitialize() {
    return findChildByClass(EntityInitialize.class);
  }

  @Override
  @Nullable
  public Expr getExpr() {
    return findChildByClass(Expr.class);
  }

  @Override
  @Nullable
  public FunctionCall getFunctionCall() {
    return findChildByClass(FunctionCall.class);
  }

  @Override
  @Nullable
  public IntExpr getIntExpr() {
    return findChildByClass(IntExpr.class);
  }

  @Override
  @Nullable
  public IsExpr getIsExpr() {
    return findChildByClass(IsExpr.class);
  }

  @Override
  @Nullable
  public JavaExpr getJavaExpr() {
    return findChildByClass(JavaExpr.class);
  }

  @Override
  @Nullable
  public ListExpression getListExpression() {
    return findChildByClass(ListExpression.class);
  }

  @Override
  @Nullable
  public SongExpr getSongExpr() {
    return findChildByClass(SongExpr.class);
  }

  @Override
  @Nullable
  public StrExpr getStrExpr() {
    return findChildByClass(StrExpr.class);
  }

  @Override
  @Nullable
  public WeightsExpr getWeightsExpr() {
    return findChildByClass(WeightsExpr.class);
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
