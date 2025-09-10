package dev.qilletni.intellij.library.ui;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBList;
import com.intellij.ui.content.ContentFactory;
import dev.qilletni.intellij.library.QilletniLibraryManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * Minimal tool window displaying installed Qilletni libraries and a Rescan action.
 */
public final class QilletniLibrariesToolWindowFactory implements ToolWindowFactory, DumbAware {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        var panel = new JPanel(new BorderLayout());
        var listModel = new DefaultListModel<String>();
        var list = new JBList<>(listModel);
        panel.add(new JScrollPane(list), BorderLayout.CENTER);

        var refreshButton = new JButton("Rescan Libraries");
        refreshButton.addActionListener(e -> {
            QilletniLibraryManager.getInstance().rescan();
            // Rescan is async; refresh list shortly after to reflect new snapshot
            var timer = new javax.swing.Timer(800, ev -> updateList(listModel));
            timer.setRepeats(false);
            timer.start();
        });
        var south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(refreshButton);
        panel.add(south, BorderLayout.SOUTH);

        updateList(listModel);

        var content = ContentFactory.getInstance().createContent(panel, "Installed", false);
        toolWindow.getContentManager().addContent(content);
    }

    private static void updateList(DefaultListModel<String> listModel) {
        listModel.clear();
        var libs = QilletniLibraryManager.getInstance().getInstalled();
        if (libs.isEmpty()) {
            listModel.addElement("No libraries found.");
            return;
        }
        for (var l : libs) {
            var ver = l.version() != null ? l.version().getVersionString() : "";
            listModel.addElement(l.name() + (ver.isBlank() ? "" : (" " + ver)) + (l.srcRoot() == null ? " (no sources)" : ""));
        }
    }
}
