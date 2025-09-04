package dev.qilletni.intellij.runconfig;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.icons.AllIcons;
import org.jetbrains.annotations.NotNull;

public final class QilletniRunConfigurationType extends ConfigurationTypeBase {
    public static final String ID = "QilletniRunConfiguration";

    public QilletniRunConfigurationType() {
        super(ID, "Qilletni", "Run Qilletni file or project", AllIcons.RunConfigurations.Application);
        addFactory(new QilletniRunConfigurationFactory(this));
    }

    public static QilletniRunConfigurationType getInstance() {
        return com.intellij.execution.configurations.ConfigurationTypeUtil.findConfigurationType(QilletniRunConfigurationType.class);
    }
}
