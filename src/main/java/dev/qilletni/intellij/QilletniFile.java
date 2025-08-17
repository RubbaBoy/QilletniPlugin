package dev.qilletni.intellij;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;

/**
 * PSI file for Qilletni.
 */
public final class QilletniFile extends PsiFileBase {
    public QilletniFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, QilletniLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return QilletniFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "Qilletni File";
    }
}