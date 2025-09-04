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

public final class QilletniBuildRunConfigurationEditor extends SettingsEditor<QilletniBuildRunConfiguration> {
    private final Project project;
    private JPanel panel;
    private TextFieldWithBrowseButton targetPath;
    private TextFieldWithBrowseButton outputDir;
    private JCheckBox buildJarCheck;
    private JCheckBox verboseGradleCheck;
    private JBTextField args;
    private EnvironmentVariablesComponent envComponent;

    public QilletniBuildRunConfigurationEditor(Project project) {
        this.project = project;
    }

    @Override
    protected void resetEditorFrom(@NotNull QilletniBuildRunConfiguration s) {
        targetPath.setText(s.targetPath);
        if (outputDir != null) outputDir.setText(s.outputDir);
        if (buildJarCheck != null) buildJarCheck.setSelected(s.buildJar);
        if (verboseGradleCheck != null) verboseGradleCheck.setSelected(s.verboseGradle);
        args.setText(s.args);
        envComponent.setEnvs(s.env);
    }

    @Override
    protected void applyEditorTo(@NotNull QilletniBuildRunConfiguration s) {
        s.targetPath = targetPath.getText();
        s.outputDir = outputDir.getText();
        s.buildJar = buildJarCheck.isSelected();
        s.verboseGradle = verboseGradleCheck.isSelected();
        s.args = args.getText();
        s.env = envComponent.getEnvs();
    }

    @Override
    protected @NotNull JComponent createEditor() {
        panel = new JPanel(new GridBagLayout());

        targetPath = new TextFieldWithBrowseButton();
        targetPath.addBrowseFolderListener(project, FileChooserDescriptorFactory.createSingleFolderDescriptor().withTitle("Project Root"));
        outputDir = new TextFieldWithBrowseButton();
        outputDir.addBrowseFolderListener(project, FileChooserDescriptorFactory.createSingleFolderDescriptor().withTitle("Output Directory"));
        buildJarCheck = new JCheckBox("Build Jar");
        verboseGradleCheck = new JCheckBox("Verbose Gradle output");
        args = new JBTextField();
        envComponent = new EnvironmentVariablesComponent();

        var row = 0;
        var gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        // Target path
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE; panel.add(new JLabel("Target path (project root):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; panel.add(targetPath, gbc); row++;

        // Output directory
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE; panel.add(new JLabel("Output directory:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; panel.add(outputDir, gbc); row++;

        // Checkboxes (right column, fill horizontally)
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; panel.add(buildJarCheck, gbc); row++;
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; panel.add(verboseGradleCheck, gbc); row++;

        // Program args
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE; panel.add(new JLabel("Program arguments:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; panel.add(args, gbc); row++;

        // Environment
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE; panel.add(new JLabel("Environment:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; panel.add(envComponent, gbc);

        return panel;
    }
}
