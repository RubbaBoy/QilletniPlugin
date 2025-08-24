package dev.qilletni.intellij;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import dev.qilletni.intellij.psi.QilletniTypes;

public final class QilletniBraceMatcher implements PairedBraceMatcher {

    private static final BracePair[] PAIRS = new BracePair[]{
        new BracePair(QilletniTypes.LEFT_PAREN, QilletniTypes.RIGHT_PAREN, false),
        new BracePair(QilletniTypes.LEFT_SBRACKET, QilletniTypes.RIGHT_SBRACKET, false),
        new BracePair(QilletniTypes.LEFT_CBRACKET, QilletniTypes.RIGHT_CBRACKET, true)
    };

    @Override
    public BracePair[] getPairs() {
        return PAIRS;
    }

    @Override
    public boolean isPairedBracesAllowedBeforeType(IElementType lbraceType, IElementType contextType) {
        // Be permissive; allow braces before any token type (common practice).
        return true;
    }

    @Override
    public int getCodeConstructStart(PsiFile file, int openingBraceOffset) {
        // Default: the construct starts at the opening brace itself.
        return openingBraceOffset;
    }
}
