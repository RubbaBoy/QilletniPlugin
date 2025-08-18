// This is a generated file. Not intended for manual editing.
package dev.qilletni.intellij.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface QilletniStmt extends PsiElement {

  @Nullable
  QilletniAsmt getAsmt();

  @Nullable
  QilletniEntityDef getEntityDef();

  @Nullable
  QilletniPlayStmt getPlayStmt();

  @Nullable
  QilletniProviderStmt getProviderStmt();

}
