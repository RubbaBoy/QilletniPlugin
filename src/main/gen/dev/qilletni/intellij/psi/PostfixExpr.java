// This is a generated file. Not intended for manual editing.
package dev.qilletni.intellij.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface PostfixExpr extends PsiElement {

  @NotNull
  List<PostfixSuffix> getPostfixSuffixList();

  @NotNull
  PrimaryExpr getPrimaryExpr();

}
