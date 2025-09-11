package dev.qilletni.intellij.project;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import dev.qilletni.intellij.library.QilletniLibraryManager;
import org.jetbrains.annotations.NotNull;

/**
 * Ensures Qilletni libraries are scanned on project startup so native.jar bindings
 * and External Libraries are available without manual rescan.
 */
public final class QilletniLibrariesStartupActivity implements StartupActivity.DumbAware {
    @Override
    public void runActivity(@NotNull Project project) {
        try {
            QilletniLibraryManager.getInstance().rescan();
        } catch (Throwable ignored) {
        }
    }
}
