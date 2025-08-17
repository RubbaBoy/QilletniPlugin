// This is a generated file. Not intended for manual editing.
package dev.qilletni.intellij.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface FunctionDef extends PsiElement {

  @Nullable
  Body getBody();

  @NotNull
  FunctionDefParams getFunctionDefParams();

  @Nullable
  FunctionOnType getFunctionOnType();

  @Nullable
  PsiElement getDocComment();

  @NotNull
  PsiElement getFunctionDef();

  @NotNull
  PsiElement getId();

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
