// This is a generated file. Not intended for manual editing.
package dev.qilletni.intellij.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface QilletniEntityBody extends PsiElement {

  @Nullable
  QilletniEntityConstructor getEntityConstructor();

  @NotNull
  List<QilletniEntityPropertyDeclaration> getEntityPropertyDeclarationList();

  @NotNull
  List<QilletniFunctionDef> getFunctionDefList();

}
