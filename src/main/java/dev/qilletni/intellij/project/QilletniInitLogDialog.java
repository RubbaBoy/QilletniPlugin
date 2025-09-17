package dev.qilletni.intellij.project;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Simple log window to display toolchain output during project initialization.
 */
public final class QilletniInitLogDialog extends DialogWrapper {
    private JTextArea area;
    private JScrollPane scroll;
    private volatile boolean open = false;

    public QilletniInitLogDialog() {
        super(/* project */ (boolean) false);
        setTitle("Qilletni Init Logs");
        initFields();
    }

    public QilletniInitLogDialog(String title) {
        super(/* project */ (boolean) false);
        setTitle(title != null ? title : "Qilletni Init Logs");
        initFields();
    }

    private void initFields() {
        area = new JTextArea(24, 100);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        scroll = new JScrollPane(area);
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        var panel = new JPanel(new BorderLayout());
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    @Override
    public void show() {
        open = true;
        super.show();
    }

    public void closeIfOpen() {
        if (open) close(OK_EXIT_CODE);
    }

    public Console getConsole() { return new Console(); }

    public void append(String text) {
        SwingUtilities.invokeLater(() -> {
            area.append(text);
            var bar = scroll.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }

    public final class Console {
        public void append(String text) { QilletniInitLogDialog.this.append(text); }
    }
}
