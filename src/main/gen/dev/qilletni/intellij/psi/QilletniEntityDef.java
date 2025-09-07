// This is a generated file. Not intended for manual editing.
package dev.qilletni.intellij.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import dev.qilletni.intellij.psi.stubs.QilletniEntityDefStub;

public interface QilletniEntityDef extends StubBasedPsiElement<QilletniEntityDefStub> {

  @NotNull
  QilletniEntityBody getEntityBody();

  @NotNull
  QilletniEntityName getEntityName();

  @Nullable
  PsiElement getDocComment();

  @NotNull
  PsiElement getEntity();

  @NotNull
  PsiElement getLeftCbracket();

  @NotNull
  PsiElement getRightCbracket();

}
