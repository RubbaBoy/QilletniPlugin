// This is a generated file. Not intended for manual editing.
package dev.qilletni.intellij.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface QilletniBody extends PsiElement {

  @NotNull
  List<QilletniBodyStmt> getBodyStmtList();

  @Nullable
  QilletniReturnStmt getReturnStmt();

}
