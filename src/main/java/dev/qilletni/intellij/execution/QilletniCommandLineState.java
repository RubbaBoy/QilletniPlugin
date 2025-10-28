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
    private QilletniBuildViewLogServer logServer;
    private final java.util.concurrent.atomic.AtomicBoolean sawProgramOutput = new java.util.concurrent.atomic.AtomicBoolean(false);

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

            if (cfg.useNativeJar) {
                params.getProgramParametersList().add("--use-native-jar");
            }
        }
        params.getProgramParametersList().add(cfg.targetPath);

        // Program args: use ParametersList parsing to respect quotes and spaces.
        if (cfg.args != null && !cfg.args.isBlank()) {
            params.getProgramParametersList().addParametersString(cfg.args);
        }

        // Determine working directory for Build View descriptor
        String workingDirectory = cfg.workingDir != null ? cfg.workingDir : getEnvironment().getProject().getBasePath();

        // Prepare per-run toolchain log server and pass port
        try {
            var title = "Building " + cfg.getName();
            logServer = new QilletniBuildViewLogServer(getEnvironment().getProject(), title, workingDirectory != null ? workingDirectory : "");
            params.getProgramParametersList().add("--log-port");
            params.getProgramParametersList().add(Integer.toString(logServer.getPort()));
        } catch (Exception e) {
            throw new ExecutionException("Failed to start Qilletni log server", e);
        }

        params.setWorkingDirectory(cfg.workingDir);
        params.setCharset(StandardCharsets.UTF_8);
        params.setEnv(cfg.env);
        return params;
    }

    @Override
    protected com.intellij.execution.process.OSProcessHandler startProcess() throws ExecutionException {
        var handler = super.startProcess();

        // Start pumping toolchain logs into the Build View
        if (logServer != null) {
            logServer.attachTo(handler);
        }

        // Show Run tool window only on first program output; otherwise print <no output> on termination
        handler.addProcessListener(new com.intellij.execution.process.ProcessAdapter() {
            private final java.util.concurrent.atomic.AtomicBoolean activated = new java.util.concurrent.atomic.AtomicBoolean(false);
            @Override
            public void onTextAvailable(@NotNull com.intellij.execution.process.ProcessEvent event, @NotNull com.intellij.openapi.util.Key outputType) {
                if (sawProgramOutput.compareAndSet(false, true)) {
                    // Activate Run tool window on first actual program output
                    var project = getEnvironment().getProject();
                    com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater(() -> {
                        var tw = com.intellij.openapi.wm.ToolWindowManager.getInstance(project).getToolWindow(com.intellij.openapi.wm.ToolWindowId.RUN);
                        if (tw != null && activated.compareAndSet(false, true)) tw.activate(null, false, true);
                    });
                }
            }

            @Override
            public void processTerminated(@NotNull com.intellij.execution.process.ProcessEvent event) {
                if (!sawProgramOutput.get()) {
                    var console = getConsoleBuilder().getConsole();
                    com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater(() ->
                            console.print("<no output>\n", com.intellij.execution.ui.ConsoleViewContentType.SYSTEM_OUTPUT));
                }
            }
        });

        return handler;
    }
}
