package dev.qilletni.intellij.runconfig;

import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.icons.AllIcons;

public final class QilletniBuildRunConfigurationType extends ConfigurationTypeBase {
    public static final String ID = "QilletniBuildRunConfiguration";

    public QilletniBuildRunConfigurationType() {
        super(ID, "Qilletni Build", "Build Qilletni library", AllIcons.Nodes.Library);
        addFactory(new QilletniBuildRunConfigurationFactory(this));
    }

    public static QilletniBuildRunConfigurationType getInstance() {
        return com.intellij.execution.configurations.ConfigurationTypeUtil.findConfigurationType(QilletniBuildRunConfigurationType.class);
    }
}
