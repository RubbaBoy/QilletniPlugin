package dev.qilletni.intellij.runconfig;

import com.intellij.execution.actions.LazyRunConfigurationProducer;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.impl.RunManagerImpl;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilCore;
import dev.qilletni.intellij.QilletniLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class QilletniRunConfigurationProducer extends LazyRunConfigurationProducer<QilletniRunConfiguration> {
    public QilletniRunConfigurationProducer() {
        super();
    }

    @Override
    protected boolean setupConfigurationFromContext(@NotNull QilletniRunConfiguration configuration, @NotNull com.intellij.execution.actions.ConfigurationContext context, @NotNull Ref<PsiElement> sourceElement) {
        PsiFile file = context.getPsiLocation() != null ? context.getPsiLocation().getContainingFile() : null;
        if (file == null || file.getVirtualFile() == null) return false;
        if (!PsiUtilCore.getLanguageAtOffset(file, 0).isKindOf(QilletniLanguage.INSTANCE)) return false;
        configuration.targetPath = file.getVirtualFile().getPath();
        configuration.setName(file.getName());
        var basePath = context.getProject().getBasePath();
        configuration.workingDir = basePath != null ? basePath : "";

        // Auto-populate Local library when target is under <projectBase>/examples/... (examples is a direct child of base)
        if (basePath != null) {
            try {
                var base = java.nio.file.Path.of(basePath).toAbsolutePath().normalize();
                var examples = base.resolve("examples");
                var target = java.nio.file.Path.of(configuration.targetPath).toAbsolutePath().normalize();
                if (target.startsWith(examples)) {
                    configuration.localLibrary = base.toString();
                }
            } catch (Exception ignored) {
                // If anything goes wrong resolving paths, leave localLibrary empty
            }
        }
        return true;
    }

    @Override
    public boolean isConfigurationFromContext(@NotNull QilletniRunConfiguration configuration, @NotNull com.intellij.execution.actions.ConfigurationContext context) {
        PsiFile file = context.getPsiLocation() != null ? context.getPsiLocation().getContainingFile() : null;
        if (file == null || file.getVirtualFile() == null) return false;
        return StringUtil.equals(configuration.targetPath, file.getVirtualFile().getPath());
    }

    @Override
    public @NotNull ConfigurationFactory getConfigurationFactory() {
        return new QilletniRunConfigurationFactory(QilletniRunConfigurationType.getInstance());
    }
}
