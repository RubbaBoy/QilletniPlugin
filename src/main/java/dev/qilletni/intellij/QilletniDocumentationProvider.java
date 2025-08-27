package dev.qilletni.intellij;

import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.lang.documentation.DocumentationMarkup;
import com.intellij.psi.PsiElement;
import dev.qilletni.intellij.doc.QilletniDocUtil;
import org.jetbrains.annotations.Nullable;

/**
 * Documentation provider for Qilletni.
 * Surfaces DOC_COMMENT text (markdown-like) as HTML for entities, functions, properties, and constructors.
 */
public class QilletniDocumentationProvider extends AbstractDocumentationProvider {
    @Override
    public @Nullable String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        String doc = QilletniDocUtil.renderHtmlDocFor(element);
        if (doc == null || doc.isEmpty()) return null;
        StringBuilder sb = new StringBuilder();
        sb.append(DocumentationMarkup.DEFINITION_START)
          .append("Documentation")
          .append(DocumentationMarkup.DEFINITION_END);
        sb.append(DocumentationMarkup.CONTENT_START)
          .append(doc)
          .append(DocumentationMarkup.CONTENT_END);
        return sb.toString();
    }
}