package dev.qilletni.intellij;

import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.lang.documentation.DocumentationMarkup;
import com.intellij.openapi.roots.AdditionalLibraryRootsProvider;
import com.intellij.openapi.roots.SyntheticLibrary;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
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
        if (doc == null || doc.isEmpty()) {
            // Try library-attached docs (scaffold): look for a docs/<name>.md in synthetic libraries
            String name = element.getText();
            if (name != null && !name.isBlank()) {
                String rel = "docs/" + name + ".md";
                for (var provider : AdditionalLibraryRootsProvider.EP_NAME.getExtensionList()) {
                    for (SyntheticLibrary lib : provider.getAdditionalProjectLibraries(element.getProject())) {
                        for (VirtualFile root : lib.getSourceRoots()) {
                            VirtualFile f = VfsUtilCore.findRelativeFile(rel, root);
                            if (f != null && f.exists() && !f.isDirectory()) {
                                // Render simple text content as-is; future: markdown rendering
                                doc = "<pre>" + f.getPath() + "</pre>";
                                break;
                            }
                        }
                        if (doc != null) break;
                    }
                    if (doc != null) break;
                }
            }
        }
        if (doc == null || doc.isEmpty()) return null;
        return DocumentationMarkup.CONTENT_START +
                doc +
                DocumentationMarkup.CONTENT_END;
    }
}