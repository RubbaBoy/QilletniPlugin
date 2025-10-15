package dev.qilletni.intellij.project;

import com.intellij.ide.wizard.AbstractNewProjectWizardStep;
import com.intellij.ide.wizard.NewProjectWizardStep;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.ui.dsl.builder.Panel;
import com.intellij.ui.dsl.builder.AlignX;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Files;
import java.util.List;

/**
 * Collects options and runs the toolchain init. Logs are shown only when errors occur.
 */
public final class QilletniNewProjectWizardStep extends AbstractNewProjectWizardStep {
    private static final Logger LOG = Logger.getInstance(QilletniNewProjectWizardStep.class);

    private JTextField authorField;
    private JRadioButton appRadio;
    private JRadioButton libRadio;
    private JCheckBox nativeToggle;
    private com.intellij.ui.components.JBTextField nativeInitClassField;
    private JPanel nativeInitPanel;

    public enum ProjectType {
        APPLICATION("application"),
        LIBRARY("library");

        private final String cliName;

        ProjectType(String cliName) {
            this.cliName = cliName;
        }

        public String getCLIName() {
            return cliName;
        }
    }

    public QilletniNewProjectWizardStep(@NotNull NewProjectWizardStep parent) {
        super(parent);
    }

    @Override
    public void setupUI(@NotNull Panel panel) {
        authorField = new JTextField(System.getProperty("user.name", ""), 24);
        appRadio = new JRadioButton("Application", true);
        libRadio = new JRadioButton("Library");
        var group = new ButtonGroup();
        group.add(appRadio);
        group.add(libRadio);
        nativeToggle = new JCheckBox();

        panel.row("Author:", row -> {
            row.cell(authorField).align(AlignX.FILL).resizableColumn();
            return kotlin.Unit.INSTANCE;
        });
        panel.row("Project type:", row -> {
            var typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            typePanel.add(appRadio);
            typePanel.add(libRadio);
            row.cell(typePanel);
            return kotlin.Unit.INSTANCE;
        });
        panel.row("Include native bindings:", row -> {
            row.cell(nativeToggle);
            return kotlin.Unit.INSTANCE;
        });
        // Native init class (FQCN) row, initially hidden
        nativeInitClassField = new com.intellij.ui.components.JBTextField();
        nativeInitClassField.getEmptyText().setText("e.g., com.example.NativeInitializer");
        nativeInitPanel = new JPanel(new BorderLayout(8, 0));
        nativeInitPanel.add(new JLabel("Native init class (FQCN):"), BorderLayout.WEST);
        nativeInitPanel.add(nativeInitClassField, BorderLayout.CENTER);
        nativeInitPanel.setVisible(false);
        panel.row("", row -> {
            row.cell(nativeInitPanel).align(AlignX.FILL).resizableColumn();
            return kotlin.Unit.INSTANCE;
        });
        nativeToggle.addActionListener(e -> {
            var visible = nativeToggle.isSelected();
            nativeInitPanel.setVisible(visible);
            if (visible) nativeInitClassField.requestFocusInWindow();
            else nativeInitClassField.setText("");
            nativeInitPanel.revalidate();
            nativeInitPanel.repaint();
        });
    }


    @Override
    public void setupProject(@NotNull com.intellij.openapi.project.Project project) {
        var ctx = getContext();
        var baseDir = ctx.getProjectDirectory();
        var projectName = ctx.getProjectName();
        var author = authorField.getText().trim();
        var type = libRadio.isSelected() ? ProjectType.LIBRARY : ProjectType.APPLICATION;
        var includeNative = nativeToggle.isSelected();
        String nativeInitClass = null;
        if (includeNative) {
            var text = nativeInitClassField != null ? nativeInitClassField.getText().trim() : "";
            if (text.isEmpty() || !isValidJavaFqcn(text)) {
                com.intellij.openapi.ui.Messages.showErrorDialog("Please enter a valid Java fully-qualified class name (no '$', identifiers separated by dots).", "Invalid Native Init Class");
                return;
            }
            nativeInitClass = text;
        }

        final String nativeInitClassFinal = nativeInitClass;
        ProgressManager.getInstance().run(new Task.Modal(null, "Creating Qilletni Project", true) {
            @Override public void run(@NotNull ProgressIndicator indicator) {
                try {
                    try { Files.createDirectories(baseDir.getParent()); } catch (Exception ignore) {}
                    var res = QilletniProjectInitRunner.runInitCapture(
                            project,
                            baseDir,
                            projectName,
                            author,
                            type,
                            includeNative,
                            nativeInitClassFinal,
                            indicator
                    );
                    var vf = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(baseDir.toFile());
                    if (vf != null) VfsUtil.markDirtyAndRefresh(false, true, true, vf);

                    if (res.success()) {
                        // Ensure module/content root exists and mark 'qilletni-src' as a Sources root
                        configureProjectRoots(project, vf, type);
                        ApplicationManager.getApplication().invokeLater(() -> QilletniPostInitOpener.openPrimaryFile(vf));
                    } else {
                        ApplicationManager.getApplication().invokeLater(() -> {
                            var dlg = new QilletniInitLogDialog("Qilletni project creation failed (" + res.exitCode() + ")");
                            dlg.append(res.output());
                            dlg.show();
                        });
                    }
                } catch (Exception ex) {
                    LOG.warn("Qilletni init failed", ex);
                    ApplicationManager.getApplication().invokeLater(() -> {
                        var dlg = new QilletniInitLogDialog("Qilletni project creation failed");
                        dlg.append(ex.getMessage() != null ? ex.getMessage() : ex.toString());
                        dlg.show();
                    });
                }
            }
        });
    }

    private void configureProjectRoots(@NotNull Project project, VirtualFile baseVf, ProjectType type) {
        if (baseVf == null || !baseVf.isDirectory()) return;

        WriteAction.runAndWait(() -> {
            // Ensure there is at least one module
            Module module;
            var moduleManager = ModuleManager.getInstance(project);
            var modules = moduleManager.getModules();
            if (modules.length == 0) {
                var basePath = project.getBasePath();
                var imlPath = (basePath != null ? basePath : baseVf.getPath()) + "/.idea/qilletni.iml";
                module = moduleManager.newModule(imlPath, StdModuleTypes.JAVA.getId());
            } else {
                module = modules[0];
            }

            var model = ModuleRootManager.getInstance(module).getModifiableModel();
            try {
                // Ensure project base is a content root
                ContentEntry contentEntry = null;
                var baseUrl = VfsUtil.pathToUrl(baseVf.getPath());
                for (var ce : model.getContentEntries()) {
                    if (ce.getUrl().equals(baseUrl)) { contentEntry = ce; break; }
                    var ceFile = ce.getFile();
                    if (ceFile != null && VfsUtil.isAncestor(ceFile, baseVf, false)) { contentEntry = ce; break; }
                }
                if (contentEntry == null) {
                    contentEntry = model.addContentEntry(baseUrl);
                    // Common excludes for cleanliness
                    var excludes = new String[]{".idea", ".git"}; // Add build/out dirs if needed later on (Qilletni doesn't really do that)
                    for (var ex : excludes) {
                        var child = baseVf.findChild(ex);
                        if (child != null) contentEntry.addExcludeFolder(child.getUrl());
                    }
                }

                // Mark sources
                var sourceDirs = switch (type) {
                    case LIBRARY -> List.of("qilletni-src", "examples");
                    case APPLICATION -> List.of("qilletni-src");
                };

                for (var sourceDir : sourceDirs) {
                    var qlSrc = baseVf.findChild(sourceDir);
                    if (qlSrc != null && qlSrc.isDirectory()) {
                        var srcUrl = qlSrc.getUrl();
                        var exists = false;
                        for (var sf : contentEntry.getSourceFolders()) {
                            if (java.util.Objects.equals(sf.getUrl(), srcUrl)) { exists = true; break; }
                        }
                        if (!exists) contentEntry.addSourceFolder(srcUrl, false);
                    }
                }
            } finally {
                // Commit model changes inside the same write action
                model.commit();
            }
        });
    }

    private static boolean isValidJavaFqcn(String s) {
        if (s == null || s.isBlank()) return false;
        if (s.indexOf('$') >= 0) return false; // no inner class dollar signs allowed
        var parts = s.split("\\.");
        if (parts.length == 0) return false;
        for (var p : parts) {
            if (p.isEmpty()) return false;
            if (!Character.isJavaIdentifierStart(p.charAt(0))) return false;
            for (int i = 1; i < p.length(); i++) {
                if (!Character.isJavaIdentifierPart(p.charAt(i))) return false;
            }
        }
        return true;
    }
}