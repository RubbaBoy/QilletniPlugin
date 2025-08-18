// This is a generated file. Not intended for manual editing.
package dev.qilletni.intellij.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface QilletniEntityDef extends PsiElement {

  @NotNull
  QilletniEntityBody getEntityBody();

  @Nullable
  PsiElement getDocComment();

  @NotNull
  PsiElement getEntity();

  @NotNull
  PsiElement getId();

  @NotNull
  PsiElement getLeftCbracket();

  @NotNull
  PsiElement getRightCbracket();

}
