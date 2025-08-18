package dev.qilletni.intellij.psi;

import com.intellij.psi.tree.IElementType;
import dev.qilletni.intellij.QilletniLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a token type in the CLIPS language.
 */
public class QilletniTokenType extends IElementType {
    public QilletniTokenType(@NotNull @NonNls String debugName) {
        super(debugName, QilletniLanguage.INSTANCE);
    }

    @Override
    public String toString() {
        return "QilletniTokenType." + super.toString();
    }
}
