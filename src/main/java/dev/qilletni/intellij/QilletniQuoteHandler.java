package dev.qilletni.intellij;

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler;
import com.intellij.psi.tree.TokenSet;
import dev.qilletni.intellij.psi.QilletniTypes;

public final class QilletniQuoteHandler extends SimpleTokenSetQuoteHandler {
    public QilletniQuoteHandler() {
        super(TokenSet.create(QilletniTypes.STRING));
    }
}
