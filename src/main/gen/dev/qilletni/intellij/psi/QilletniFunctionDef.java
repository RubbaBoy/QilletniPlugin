// This is a generated file. Not intended for manual editing.
package dev.qilletni.intellij.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import dev.qilletni.intellij.psi.stubs.QilletniFunctionDefStub;

public interface QilletniFunctionDef extends StubBasedPsiElement<QilletniFunctionDefStub> {

  @Nullable
  QilletniBody getBody();

  @NotNull
  QilletniFunctionDefParams getFunctionDefParams();

  @NotNull
  QilletniFunctionName getFunctionName();

  @Nullable
  QilletniFunctionOnType getFunctionOnType();

  @Nullable
  PsiElement getDocComment();

  @NotNull
  PsiElement getFunctionDef();

  @Nullable
  PsiElement getLeftCbracket();

  @NotNull
  PsiElement getLeftParen();

  @Nullable
  PsiElement getNative();

  @Nullable
  PsiElement getRightCbracket();

  @NotNull
  PsiElement getRightParen();

  @Nullable
  PsiElement getStatic();

}
