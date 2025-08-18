// This is a generated file. Not intended for manual editing.
package dev.qilletni.intellij.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface QilletniProviderStmt extends PsiElement {

  @Nullable
  QilletniBody getBody();

  @NotNull
  QilletniStrExpr getStrExpr();

  @Nullable
  PsiElement getLeftCbracket();

  @NotNull
  PsiElement getProvider();

  @Nullable
  PsiElement getRightCbracket();

}
