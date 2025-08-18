package dev.qilletni.intellij;

import com.intellij.lexer.FlexAdapter;
import dev.qilletni.intellij.lexer.QilletniLexer;

import java.io.Reader;

/**
 * Lexer adapter that wraps the generated JFlex lexer for Qilletni.
 */
public final class QilletniLexerAdapter extends FlexAdapter {
    public QilletniLexerAdapter() {
        super(new QilletniLexer((Reader) null));
    }
}
