package dev.qilletni.intellij;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * File type for Qilletni .ql files.
 */
public final class QilletniFileType extends LanguageFileType {
    public static final QilletniFileType INSTANCE = new QilletniFileType();

    private QilletniFileType() {
        super(QilletniLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "Qilletni";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Qilletni source file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "ql";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return QilletniIcons.FILE;
    }
}