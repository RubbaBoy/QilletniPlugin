package dev.qilletni.intellij.toolchain;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Resolves and validates the Qilletni CLI/toolchain executable for a project.
 * Detection order: project override > application default > PATH probing.
 */
public final class QilletniToolchainService {
    private static final Logger LOG = Logger.getInstance(QilletniToolchainService.class);

    private final Project project;
    private final CachedValue<ValidationResult> versionInfo;

    public QilletniToolchainService(@NotNull Project project) {
        this.project = project;
        this.versionInfo = CachedValuesManager.getManager(project).createCachedValue(() -> {
            var appTracker = QilletniToolchainSettings.getInstance().getModificationTracker();
            var projectTracker = QilletniToolchainSettingsProject.getInstance(project).getModificationTracker();
            return CachedValueProvider.Result.create(fetchValidation(), appTracker, projectTracker);
        }, false);
    }

    public static QilletniToolchainService getInstance(@NotNull Project project) {
        return project.getService(QilletniToolchainService.class);
    }

    public @Nullable File getExecutable() {
        var settings = QilletniToolchainSettings.getInstance();
        var projectPath = QilletniToolchainSettingsProject.getInstance(project).getState().toolchainPath;
        var candidate = firstNonBlank(projectPath, settings.getState().toolchainPath);
        if (candidate != null) {
            var f = new File(candidate);
            if (f.isFile() && f.canExecute()) return f;
            var bin = new File(candidate, binName());
            if (bin.isFile() && bin.canExecute()) return bin;
        }

        for (var name : executableNames()) {
            var onPath = findOnPath(name);
            if (onPath != null) return onPath;
        }
        return null;
    }

    public @NotNull ValidationResult validate() {
        var v = versionInfo.getValue();
        return v != null ? v : new ValidationResult(false, null, "Qilletni executable not found");
    }

    public @Nullable @NlsSafe String getVersion() {
        var v = validate();
        return v.ok ? v.version : null;
    }

    private ValidationResult fetchValidation() {
        try {
            var exe = getExecutable();
            if (exe == null) return new ValidationResult(false, null, "Executable not found");
            var cmd = new GeneralCommandLine(exe.getAbsolutePath());
            cmd.addParameter("--version");
            cmd.withCharset(StandardCharsets.UTF_8);
            var process = cmd.createProcess();
            if (!process.waitFor(5, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return new ValidationResult(false, null, "Version query timed out");
            }
            var out = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            if (out.isEmpty()) out = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            if (out.isEmpty()) return new ValidationResult(false, null, "No version output");
            return new ValidationResult(true, out, null);
        } catch (Exception e) {
            LOG.warn("Failed to validate Qilletni toolchain", e);
            return new ValidationResult(false, null, e.getMessage());
        }
    }

    private static String binName() {
        // Use this when using actually installed alias (if ever?)
//        return SystemInfo.isWindows ? "qilletni.bat" : "qilletni";
        return "QilletniToolchain.jar";
    }

    private static List<String> executableNames() {
        // Use this when using actually installed alias (if ever?)
//        return SystemInfo.isWindows ? List.of("qilletni.exe", "qilletni.cmd", "qilletni.bat") : List.of("qilletni");
        return List.of("QilletniToolchain.jar");
    }

    private static @Nullable File findOnPath(String name) {
        var path = System.getenv("PATH");
        if (path == null || path.isBlank()) return null;
        var sep = SystemInfo.isWindows ? ";" : ":";
        for (var dir : path.split(sep)) {
            var f = new File(dir, name);
            if (f.isFile() && f.canExecute()) return f;
        }
        return null;
    }

    private static @Nullable String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        return null;
    }

    public record ValidationResult(boolean ok, @Nullable String version, @Nullable String message) { }
}
