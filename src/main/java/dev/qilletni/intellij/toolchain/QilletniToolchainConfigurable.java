package dev.qilletni.intellij.toolchain;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class QilletniToolchainConfigurable implements Configurable {
    private JPanel panel;
    private TextFieldWithBrowseButton appPath;
    private JButton checkButton;
    private JLabel versionLabel;

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
        return "Qilletni: Toolchain";
    }

    @Override
    public @Nullable JComponent createComponent() {
        panel = new JPanel(new GridBagLayout());
        appPath = new TextFieldWithBrowseButton();
        appPath.addBrowseFolderListener(null, com.intellij.openapi.fileChooser.FileChooserDescriptorFactory.createSingleFileDescriptor("jar").withTitle("Select Qilletni executable"));

        checkButton = new JButton("Check version");
        versionLabel = new JLabel(" "); // keep height
        versionLabel.setForeground(UIManager.getColor("Label.foreground"));

        var gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Executable path (application default):"), gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(appPath, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        panel.add(checkButton, gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 2; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(versionLabel, gbc);

        // Clear status when path changes
        appPath.getTextField().getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void clear() { setStatus(null, null); }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { clear(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { clear(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { clear(); }
        });

        checkButton.addActionListener(e -> runVersionCheck());

        return panel;
    }

    @Override
    public boolean isModified() {
        var state = QilletniToolchainSettings.getInstance().getState();
        var cur = state != null ? state.toolchainPath : null;
        var val = appPath.getText();
        return cur == null ? (val != null && !val.isBlank()) : !Objects.equals(cur, val);
    }

    @Override
    public void apply() {
        var settings = QilletniToolchainSettings.getInstance();
        var s = settings.getState();
        if (s != null) s.toolchainPath = appPath.getText();
    }

    @Override
    public void reset() {
        var s = QilletniToolchainSettings.getInstance().getState();
        appPath.setText(s != null ? s.toolchainPath : "");
        setStatus(null, null);
    }

    private void setStatus(@Nullable String text, @Nullable Color color) {
        if (text == null || text.isBlank()) {
            versionLabel.setText(" ");
            versionLabel.setForeground(UIManager.getColor("Label.foreground"));
        } else {
            versionLabel.setText(text);
            versionLabel.setForeground(color != null ? color : UIManager.getColor("Label.foreground"));
        }
    }

    private void runVersionCheck() {
        final var jarPath = appPath.getText();
        if (jarPath == null || jarPath.isBlank()) {
            setStatus("No executable selected", new Color(0xD32F2F));
            return;
        }

        // Resolve Java SDK like the Run Configuration: prefer Project SDK from an open project; fall back to any Java SDK.
        Sdk jdk = null;
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        if (projects.length > 0) {
            jdk = ProjectRootManager.getInstance(projects[0]).getProjectSdk();
        }
        if (jdk == null) {
            var javaSdks = ProjectJdkTable.getInstance().getSdksOfType(JavaSdk.getInstance());
            if (!javaSdks.isEmpty()) jdk = javaSdks.get(0);
        }
        if (jdk == null) {
            setStatus("No Java SDK configured", new Color(0xD32F2F));
            return;
        }

        String javaExe = JavaSdk.getInstance().getVMExecutablePath(jdk);
        if (javaExe == null || javaExe.isBlank()) {
            setStatus("Java executable not found for SDK", new Color(0xD32F2F));
            return;
        }

        setStatus("Checking...", UIManager.getColor("Label.foreground"));

        // Run java -jar <jar> --version off the EDT
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                var cmd = new GeneralCommandLine(javaExe)
                        .withParameters("-jar", jarPath, "--version")
                        .withCharset(StandardCharsets.UTF_8);

                ProcessOutput out = new CapturingProcessHandler(cmd).runProcess(15000, false);
                if (out.getExitCode() == 0) {
                    String stdout = out.getStdout().trim();
                    if (stdout.isEmpty()) stdout = "OK (no output)";
                    final String msg = "Version: " + stdout;
                    javax.swing.SwingUtilities.invokeLater(() -> setStatus(msg, new Color(0x3FD32F)));
                } else {
                    String err = out.getStderr().isBlank() ? out.getStdout() : out.getStderr();
                    final String msg = "Error: " + (err == null ? "unknown" : err.trim());
                    javax.swing.SwingUtilities.invokeLater(() -> setStatus(msg, new Color(0xD32F2F)));
                }
            } catch (Throwable t) {
                final String msg = "Error: " + t.getMessage();
                javax.swing.SwingUtilities.invokeLater(() -> setStatus(msg, new Color(0xD32F2F)));
            }
        });
    }
}
