package dev.qilletni.intellij;

import com.intellij.lang.Language;

/**
 * Qilletni language ID holder.
 */
public final class QilletniLanguage extends Language {
    public static final QilletniLanguage INSTANCE = new QilletniLanguage();

    private QilletniLanguage() {
        super("Qilletni");
    }
}