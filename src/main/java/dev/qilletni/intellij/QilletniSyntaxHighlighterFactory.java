package dev.qilletni.intellij;

import com.intellij.openapi.fileTypes.SingleLazyInstanceSyntaxHighlighterFactory;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import org.jetbrains.annotations.NotNull;

/**
 * Factory for Qilletni syntax highlighter.
 */
public final class QilletniSyntaxHighlighterFactory extends SingleLazyInstanceSyntaxHighlighterFactory {
    @Override
    protected @NotNull SyntaxHighlighter createHighlighter() {
        return new QilletniSyntaxHighlighter();
        }
}
