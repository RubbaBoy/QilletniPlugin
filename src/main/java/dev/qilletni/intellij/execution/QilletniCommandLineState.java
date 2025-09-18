package dev.qilletni.intellij.execution;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.JavaCommandLineState;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.roots.ProjectRootManager;
import dev.qilletni.intellij.runconfig.QilletniRunConfiguration;
import dev.qilletni.intellij.toolchain.QilletniToolchainSettings;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

public final class QilletniCommandLineState extends JavaCommandLineState {
    private final QilletniRunConfiguration cfg;

    public QilletniCommandLineState(@NotNull ExecutionEnvironment env, @NotNull QilletniRunConfiguration cfg) {
        super(env);
        this.cfg = cfg;
    }

    @Override
    protected JavaParameters createJavaParameters() throws ExecutionException {
        var params = new JavaParameters();

        var projectSdk = ProjectRootManager.getInstance(getEnvironment().getProject()).getProjectSdk();
        if (projectSdk == null) {
            throw new ExecutionException("No Project SDK configured.");
        }
        params.setJdk(projectSdk);

        // Resolve SDK JAR from Toolchain settings, put it on the classpath, and run the main class.
        var toolchainState = QilletniToolchainSettings.getInstance().getState();
        var sdkJarPath = toolchainState != null ? toolchainState.toolchainPath : null;
        if (sdkJarPath == null || sdkJarPath.isBlank()) {
            throw new ExecutionException("Qilletni SDK JAR not configured. Configure it in Settings | Qilletni: Toolchain.");
        }
        params.getClassPath().add(sdkJarPath);
        params.setMainClass("dev.qilletni.toolchain.QilletniToolchainApplication");

        params.getProgramParametersList().add("run");
        // Inject optional local library before the target path
        if (cfg.localLibrary != null && !cfg.localLibrary.isBlank()) {
            var absoluteLib = java.nio.file.Path.of(cfg.localLibrary).toAbsolutePath().normalize().toString();
            params.getProgramParametersList().add("--local-library");
            params.getProgramParametersList().add(absoluteLib);
        }
        params.getProgramParametersList().add(cfg.targetPath);

        // Program args: use ParametersList parsing to respect quotes and spaces.
        if (cfg.args != null && !cfg.args.isBlank()) {
            params.getProgramParametersList().addParametersString(cfg.args);
        }

        params.setWorkingDirectory(cfg.workingDir);
        params.setCharset(StandardCharsets.UTF_8);
        params.setEnv(cfg.env);
        return params;
    }
}
