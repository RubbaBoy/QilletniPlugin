package dev.qilletni.intellij.runconfig;

import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.JDOMExternalizerUtil;
import com.intellij.openapi.util.text.StringUtil;
import dev.qilletni.intellij.execution.QilletniBuildCommandLineState;
import dev.qilletni.intellij.toolchain.QilletniToolchainService;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class QilletniBuildRunConfiguration extends RunConfigurationBase<Object> {
    public String targetPath = "";
    public String args = "";
    public Map<String, String> env = new HashMap<>();
    public String toolchainOverride = "";
    public String outputDir = "";
    public boolean buildJar = false;
    public boolean verboseGradle = false;

    protected QilletniBuildRunConfiguration(@NotNull Project project, @NotNull ConfigurationFactory factory, String name) {
        super(project, factory, name);
    }

    @Override
    public @NotNull SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new QilletniBuildRunConfigurationEditor(getProject());
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        if (StringUtil.isEmptyOrSpaces(targetPath)) throw new RuntimeConfigurationError("Target path is required");
        var file = new File(targetPath);
        if (!file.exists()) throw new RuntimeConfigurationError("Target does not exist");

        if (StringUtil.isEmptyOrSpaces(outputDir)) {
            throw new RuntimeConfigurationError("Output directory is required");
        }

        var toolchain = QilletniToolchainService.getInstance(getProject());
        var result = toolchain.validate();
        if (!result.ok()) throw new RuntimeConfigurationError(result.message() != null ? result.message() : "Invalid toolchain");
    }

    @Override
    public @Nullable RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) {
        return new QilletniBuildCommandLineState(environment, this);
    }

    @Override
    public void readExternal(@NotNull Element element) {
        super.readExternal(element);
        targetPath = JDOMExternalizerUtil.readField(element, "targetPath", "");
        args = JDOMExternalizerUtil.readField(element, "args", "");
        toolchainOverride = JDOMExternalizerUtil.readField(element, "toolchainOverride", "");
        outputDir = JDOMExternalizerUtil.readField(element, "outputDir", "");
        buildJar = Boolean.parseBoolean(JDOMExternalizerUtil.readField(element, "buildJar", "false"));
        verboseGradle = Boolean.parseBoolean(JDOMExternalizerUtil.readField(element, "verboseGradle", "false"));
        // env is serialized by the UI component in the editor if present
    }

    @Override
    public void writeExternal(@NotNull Element element) {
        super.writeExternal(element);
        JDOMExternalizerUtil.writeField(element, "targetPath", targetPath);
        JDOMExternalizerUtil.writeField(element, "args", args);
        JDOMExternalizerUtil.writeField(element, "toolchainOverride", toolchainOverride);
        JDOMExternalizerUtil.writeField(element, "outputDir", outputDir);
        JDOMExternalizerUtil.writeField(element, "buildJar", Boolean.toString(buildJar));
        JDOMExternalizerUtil.writeField(element, "verboseGradle", Boolean.toString(verboseGradle));
    }
}
