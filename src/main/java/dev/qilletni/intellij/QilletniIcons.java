package dev.qilletni.intellij;

import com.intellij.openapi.util.IconLoader;
import javax.swing.*;

/**
 * Centralized icon access for Qilletni plugin.
 */
public final class QilletniIcons {
    private QilletniIcons() {}

    public static final Icon FILE = IconLoader.getIcon("/META-INF/pluginIcon.svg", QilletniIcons.class);
}