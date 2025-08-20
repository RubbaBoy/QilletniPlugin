package dev.qilletni.intellij.refactoring;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;
import dev.qilletni.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Ensures entity and constructor identifiers stay in sync on rename:
 * - Renaming an entity will also rename its constructor if present.
 * - Renaming a constructor will also rename its enclosing entity.
 * This processor avoids recursion by using prepareRenaming single-pass augmentation.
 */
public final class QilletniRenameEntityAndConstructorProcessor extends RenamePsiElementProcessor {
    @Override
    public boolean canProcessElement(@NotNull PsiElement element) {
        return element instanceof QilletniEntityName || element instanceof QilletniConstructorName;
    }

    @Override
    public void prepareRenaming(@NotNull PsiElement element, @NotNull String newName, @NotNull Map<PsiElement, String> allRenames) {
        if (element instanceof QilletniEntityName entityName) {
            // Find constructor inside the same entity_def
            var entityDef = PsiTreeUtil.getParentOfType(entityName, QilletniEntityDef.class);
            if (entityDef != null) {
                var ctor = PsiTreeUtil.findChildOfType(entityDef, QilletniConstructorName.class);
                if (ctor != null && !allRenames.containsKey(ctor)) {
                    allRenames.put(ctor, newName);
                }
            }
        } else if (element instanceof QilletniConstructorName ctorName) {
            // Find enclosing entity and its name
            var entityDef = PsiTreeUtil.getParentOfType(ctorName, QilletniEntityDef.class);
            if (entityDef != null) {
                var eName = PsiTreeUtil.findChildOfType(entityDef, QilletniEntityName.class);
                if (eName != null && !allRenames.containsKey(eName)) {
                    allRenames.put(eName, newName);
                }
            }
        }
    }
}
