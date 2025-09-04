package dev.qilletni.intellij.runconfig;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Paths;

public final class QilletniBuildRunConfigurationFactory extends ConfigurationFactory {
    protected QilletniBuildRunConfigurationFactory(@NotNull ConfigurationType type) {
        super(type);
    }

    @Override
    public @NotNull String getId() {
        return QilletniBuildRunConfigurationType.ID;
    }

    @Override
    public @NotNull RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        var cfg = new QilletniBuildRunConfiguration(project, this, "Qilletni Build");
        var base = project.getBasePath();
        cfg.targetPath = base != null ? base : "";
        cfg.outputDir = Paths.get(System.getProperty("user.home"), ".qilletni", "packages").toString();
        return cfg;
    }
}
