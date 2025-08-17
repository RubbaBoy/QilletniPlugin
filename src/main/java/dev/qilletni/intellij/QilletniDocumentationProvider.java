package dev.qilletni.intellij;

import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

/**
 * Basic documentation provider for Qilletni. Will later surface DOC_COMMENT text from PSI.
 */
public class QilletniDocumentationProvider extends AbstractDocumentationProvider {
    @Override
    public @Nullable String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        // Placeholder: Will be implemented once PSI for DOC_COMMENT exists.
        return null;
    }
}