package dev.qilletni.intellij.runconfig;

import com.intellij.execution.configuration.EnvironmentVariablesComponent;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.JBTextField;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public final class QilletniRunConfigurationEditor extends SettingsEditor<QilletniRunConfiguration> {
    private final Project project;
    private JPanel panel;
    private TextFieldWithBrowseButton targetPath;
    private JBTextField args;
    private TextFieldWithBrowseButton workingDir;
    private EnvironmentVariablesComponent envComponent;

    public QilletniRunConfigurationEditor(Project project) {
        this.project = project;
    }

    @Override
    protected void resetEditorFrom(@NotNull QilletniRunConfiguration s) {
        targetPath.setText(s.targetPath);
        args.setText(s.args);
        workingDir.setText(s.workingDir);
        envComponent.setEnvs(s.env);
    }

    @Override
    protected void applyEditorTo(@NotNull QilletniRunConfiguration s) {
        s.targetPath = targetPath.getText();
        s.args = args.getText();
        s.workingDir = workingDir.getText();
        s.env = envComponent.getEnvs();
    }

    @Override
    protected @NotNull JComponent createEditor() {
        panel = new JPanel(new GridBagLayout());
        targetPath = new TextFieldWithBrowseButton();
        targetPath.addBrowseFolderListener(project, FileChooserDescriptorFactory.createSingleFileDescriptor("ql").withTitle("Select target (.ql or directory)"));
        args = new JBTextField();
        workingDir = new TextFieldWithBrowseButton();
        workingDir.addBrowseFolderListener(project, FileChooserDescriptorFactory.createSingleFolderDescriptor().withTitle("Working Directory"));
        envComponent = new EnvironmentVariablesComponent();

        var row = 0;
        var gbc = new GridBagConstraints();
        gbc.insets = new Insets(4,4,4,4);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel("Target path:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; panel.add(targetPath, gbc); row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE; panel.add(new JLabel("Program arguments:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; panel.add(args, gbc); row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE; panel.add(new JLabel("Working directory:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; panel.add(workingDir, gbc); row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE; panel.add(new JLabel("Environment:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; panel.add(envComponent, gbc);

        return panel;
    }
}
