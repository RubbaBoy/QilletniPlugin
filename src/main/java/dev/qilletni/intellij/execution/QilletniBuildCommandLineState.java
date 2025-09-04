package dev.qilletni.intellij.execution;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.JavaCommandLineState;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.roots.ProjectRootManager;
import dev.qilletni.intellij.runconfig.QilletniBuildRunConfiguration;
import dev.qilletni.intellij.toolchain.QilletniToolchainSettings;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

public final class QilletniBuildCommandLineState extends JavaCommandLineState {
    private final QilletniBuildRunConfiguration cfg;

    public QilletniBuildCommandLineState(@NotNull ExecutionEnvironment env, @NotNull QilletniBuildRunConfiguration cfg) {
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

        // Build-specific leading command
        params.getProgramParametersList().add("build");
        params.getProgramParametersList().add(cfg.targetPath);

        // Output directory argument
        if (cfg.outputDir != null && !cfg.outputDir.isBlank()) {
            params.getProgramParametersList().add("--output-file");
            params.getProgramParametersList().add(cfg.outputDir);
        }

        if (cfg.buildJar) {
            params.getProgramParametersList().add("--no-build-jar");
        }

        if (cfg.verboseGradle) {
            params.getProgramParametersList().add("--verbose");
        }

        // Additional program args from the configuration
        if (cfg.args != null && !cfg.args.isBlank()) {
            params.getProgramParametersList().addParametersString(cfg.args);
        }

        // Use targetPath as working directory; fall back to project base if needed
        String wd = (cfg.targetPath != null && !cfg.targetPath.isBlank())
                ? cfg.targetPath
                : getEnvironment().getProject().getBasePath();
        if (wd != null && !wd.isBlank()) {
            params.setWorkingDirectory(wd);
        }

        params.setCharset(StandardCharsets.UTF_8);
        params.setEnv(cfg.env);
        return params;
    }
}
