package dev.qilletni.intellij.project;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ProjectRootManager;
import dev.qilletni.intellij.toolchain.QilletniToolchainSettings;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs the Qilletni toolchain 'init' command and shows logs in a dialog window.
 */
public final class QilletniProjectInitRunner {
    private static final Logger LOG = Logger.getInstance(QilletniProjectInitRunner.class);

    public record Result(boolean success, int exitCode, String output) { }

    public static Result runInitCapture(@NotNull Project project,
                                        @NotNull Path targetDir,
                                        @NotNull String projectName,
                                        @NotNull String author,
                                        QilletniNewProjectWizardStep.ProjectType type,
                                        boolean includeNative,
                                        String nativeInitClass,
                                        ProgressIndicator indicator) throws Exception {
        var sdkState = QilletniToolchainSettings.getInstance().getState();
        var sdkJar = sdkState != null ? sdkState.toolchainPath : null;
        if (sdkJar == null || sdkJar.isBlank()) {
            return new Result(false, -1, "Qilletni SDK JAR not configured. Configure it in Settings | Qilletni: Toolchain.");
        }

        var cmd = buildJavaToolchainCommand(project, new File(sdkJar), buildInitArgs(targetDir, projectName, author, type, includeNative, nativeInitClass));
        var handler = new CapturingProcessHandler(cmd);
        if (indicator != null) indicator.setText("Running: " + cmd.getCommandLineString());
        ProcessOutput out = handler.runProcessWithProgressIndicator(indicator);
        int exit = out.getExitCode();
        String combined = (out.getStdout() == null ? "" : out.getStdout()) + (out.getStderr() == null ? "" : out.getStderr());
        return new Result(exit == 0, exit, combined);
    }

    public static Result runInitWithLogs(@NotNull Project project,
                                         @NotNull Path targetDir,
                                         @NotNull String projectName,
                                         @NotNull String author,
                                         @NotNull QilletniNewProjectWizardStep.ProjectType type,
                                         boolean includeNative,
                                         String nativeInitClass,
                                         ProgressIndicator indicator) throws Exception {
        var result = runInitCapture(project, targetDir, projectName, author, type, includeNative, nativeInitClass, indicator);
        SwingUtilities.invokeLater(() -> {
            var dlg = new QilletniInitLogDialog(result.success() ? "Qilletni Init Logs" : ("Qilletni init failed (" + result.exitCode() + ")"));
            dlg.append(result.output());
            dlg.show();
        });
        return result;
    }

    private static List<String> buildInitArgs(Path targetDir, String name, String author, QilletniNewProjectWizardStep.ProjectType type, boolean includeNative, String nativeInitClass) {
        var p = new ArrayList<String>();
        p.add("init");
        p.add(targetDir.toAbsolutePath().toString());
        p.add("--name"); p.add(name);
        p.add("--author"); p.add(author);
        p.add("--type"); p.add(type.getCLIName());
        if (!includeNative) {
            p.add("--no-native");
        } else if (nativeInitClass != null && !nativeInitClass.isBlank()) {
            p.add("--native-class");
            p.add(nativeInitClass);
        }
        return p;
    }

    private static GeneralCommandLine buildJavaToolchainCommand(Project project, File sdkJar, List<String> args) {
        var sdk = ProjectRootManager.getInstance(project).getProjectSdk();
        if (sdk == null) {
            var defaultProject = ProjectManager.getInstance().getDefaultProject();
            var defaultSdk = ProjectRootManager.getInstance(defaultProject).getProjectSdk();
            if (defaultSdk != null) sdk = defaultSdk;
        }
        if (sdk == null) {
            throw new IllegalStateException("No Project SDK configured. Set Project SDK (File | Project Structure) or Default Project SDK (File | New Projects Setup). ");
        }
        var home = sdk.getHomePath();
        if (home == null || home.isBlank()) {
            throw new IllegalStateException("Project SDK home is not set.");
        }
        var javaExe = new File(new File(home, "bin"), com.intellij.openapi.util.SystemInfo.isWindows ? "java.exe" : "java");
        var cmd = new GeneralCommandLine(javaExe.getAbsolutePath());
        cmd.withCharset(StandardCharsets.UTF_8);
        cmd.addParameter("-cp");
        cmd.addParameter(sdkJar.getAbsolutePath());
        cmd.addParameter("dev.qilletni.toolchain.QilletniToolchainApplication");
        for (var a : args) cmd.addParameter(a);
        return cmd;
    }
}
