package dev.qilletni.intellij.project;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public final class QilletniPostInitOpener {
    private QilletniPostInitOpener() {}

    public static void openPrimaryFile(@Nullable VirtualFile baseDir) {
        if (baseDir == null) return;
        Project project = com.intellij.openapi.project.ProjectManager.getInstance().getOpenProjects().length > 0
                ? com.intellij.openapi.project.ProjectManager.getInstance().getOpenProjects()[0] : null;
        if (project == null) return;

        // Try README.md
        var readme = baseDir.findChild("README.md");
        if (readme != null) { FileEditorManager.getInstance(project).openFile(readme, true); return; }
        // Try qilletni_info.yml in qilletni-src
        var src = baseDir.findChild("qilletni-src");
        if (src != null) {
            var info = src.findChild("qilletni_info.yml");
            if (info != null) { FileEditorManager.getInstance(project).openFile(info, true); return; }
            // else open the first .ql file if any
            for (var child : src.getChildren()) {
                if (!child.isDirectory() && child.getName().endsWith(".ql")) {
                    FileEditorManager.getInstance(project).openFile(child, true); return;
                }
            }
        }
        // Fallback: open the base directory in Project View implicitly by focusing it
        // No-op if nothing specific to open
    }
}
