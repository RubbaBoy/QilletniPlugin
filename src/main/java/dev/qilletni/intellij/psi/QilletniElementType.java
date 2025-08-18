package dev.qilletni.intellij.psi;

import com.intellij.psi.tree.IElementType;
import dev.qilletni.intellij.QilletniLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class QilletniElementType extends IElementType {
    public QilletniElementType(@NonNls @NotNull String debugName) {
        super(debugName, QilletniLanguage.INSTANCE);
    }
}
