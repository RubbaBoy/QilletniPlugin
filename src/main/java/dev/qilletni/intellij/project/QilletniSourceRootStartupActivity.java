package dev.qilletni.intellij.project;

import com.intellij.ProjectTopics;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Startup activity that detects Qilletni source roots and prompts the user to mark them as Sources Root.
 * Uses official ModuleRootManager APIs for applying changes (latest IDEs still support and recommend this path for simple cases).
 */
public final class QilletniSourceRootStartupActivity implements StartupActivity.DumbAware {
    private static final String QILLETNI_DIR = "qilletni-src";
    private static final String NOTIFICATION_GROUP_ID = "Qilletni Source Root";
    private static final String DONT_ASK_KEY_PREFIX = "qilletni.srcroot.dismissed."; // per-module flag

    @Override
    public void runActivity(@NotNull Project project) {
        detectAndPrompt(project);
        // Ensure QLL libraries are visible under External Libraries on startup
        try {
            var settings = dev.qilletni.intellij.library.QilletniLibrariesSettings.getInstance();
            if (settings != null && settings.getLibrariesRootPath() != null && !settings.getLibrariesRootPath().isBlank()) {
                // Kick off a rescan (async). When it completes, QilletniLibraryManager will sync Project Libraries.
                dev.qilletni.intellij.library.QilletniLibraryManager.getInstance().rescan();
            } else {
                // No configured path, but still sync to remove any old mirrored libraries.
                dev.qilletni.intellij.library.QilletniProjectLibraries.syncProject(project);
            }
        } catch (Throwable ignored) {}

        // Listen for project roots changes (modules/content roots added or removed)
        project.getMessageBus().connect(project).subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootListener() {
            @Override
            public void rootsChanged(@NotNull ModuleRootEvent event) {
                dev.qilletni.intellij.resolve.QilletniAliasResolver.onRootsChanged();
                detectAndPrompt(project);
            }
        });

        // Listen for VFS changes where qilletni-src or .ql files appear
        project.getMessageBus().connect(project).subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
            @Override
            public void after(@NotNull List<? extends @NotNull VFileEvent> events) {
                for (var e : events) {
                    var f = e.getFile();
                    if (f != null && (QILLETNI_DIR.equals(f.getName()) || f.getName().endsWith(".ql"))) {
                        dev.qilletni.intellij.resolve.QilletniAliasResolver.onRootsChanged();
                        detectAndPrompt(project);
                        break;
                    }
                }
            }
        });
    }

    private record Candidate(Module module, VirtualFile dir) {}

    private void detectAndPrompt(@NotNull Project project) {
        var candidates = findCandidates(project);
        if (candidates.isEmpty()) return;

        var notification = NotificationGroupManager.getInstance()
                .getNotificationGroup(NOTIFICATION_GROUP_ID)
                .createNotification(
                        "Configure Qilletni sources",
                        "Mark 'qilletni-src' folder(s) as Sources Root for proper indexing and navigation.",
                        NotificationType.INFORMATION
                );

        notification.addAction(NotificationAction.createSimple("Apply", () -> {
            applyCandidates(candidates);
            notification.expire();
        }));
        notification.addAction(NotificationAction.createSimple("Don't ask again", () -> {
            rememberDismissed(candidates);
            notification.expire();
        }));
        notification.addAction(NotificationAction.createSimple("Dismiss", notification::expire));

        notification.notify(project);
    }

    private void rememberDismissed(@NotNull List<Candidate> candidates) {
        var props = PropertiesComponent.getInstance();
        for (var c : candidates) props.setValue(dontAskKey(c.module()), true);
    }

    private @NotNull List<Candidate> findCandidates(@NotNull Project project) {
        var result = new ArrayList<Candidate>();
        var props = PropertiesComponent.getInstance();
        for (var module : ModuleManager.getInstance(project).getModules()) {
            if (props.getBoolean(dontAskKey(module))) continue;
            var contentRoots = ModuleRootManager.getInstance(module).getContentRoots();
            var existingSourceUrls = new HashSet<String>();
            for (var sf : ModuleRootManager.getInstance(module).getSourceRoots(false)) existingSourceUrls.add(sf.getUrl());

            for (var contentRoot : contentRoots) {
                var ql = contentRoot.findChild(QILLETNI_DIR);
                if (ql != null && ql.isDirectory()) {
                    if (!existingSourceUrls.contains(ql.getUrl())) result.add(new Candidate(module, ql));
                }
            }
        }
        return result;
    }


    private void applyCandidates(@NotNull List<Candidate> candidates) {
        if (candidates.isEmpty()) return;
        // Group by module to minimize model commits
        Map<Module, List<VirtualFile>> byModule = new HashMap<>();
        for (var c : candidates) byModule.computeIfAbsent(c.module(), k -> new ArrayList<>()).add(c.dir());

        for (var entry : byModule.entrySet()) {
            var module = entry.getKey();
            var model = ModuleRootManager.getInstance(module).getModifiableModel();
            try {
                for (var dir : entry.getValue()) {
                    addSourceFolderIfMissing(model, dir);
                }
            } finally {
                WriteAction.run(model::commit);
            }
        }
    }

    private static void addSourceFolderIfMissing(@NotNull ModifiableRootModel model, @NotNull VirtualFile dir) {
        ContentEntry targetEntry = findOrCreateContentEntry(model, dir);
        var url = dir.getUrl();
        for (var sf : targetEntry.getSourceFolders()) {
            if (Objects.equals(sf.getUrl(), url)) return; // already marked
        }
        targetEntry.addSourceFolder(url, false);
        // Exclude nested IDE metadata directories inside source root to avoid index filter warnings
        var ideaDir = dir.findChild(".idea");
        if (ideaDir != null && ideaDir.isDirectory()) {
            boolean alreadyExcluded = false;
            for (var ex : targetEntry.getExcludeFolders()) {
                if (java.util.Objects.equals(ex.getUrl(), ideaDir.getUrl())) { alreadyExcluded = true; break; }
            }
            if (!alreadyExcluded) {
                targetEntry.addExcludeFolder(ideaDir.getUrl());
            }
        }
    }

    private static ContentEntry findOrCreateContentEntry(@NotNull ModifiableRootModel model, @NotNull VirtualFile fileOrDir) {
        for (var ce : model.getContentEntries()) {
            var ceFile = ce.getFile();
            if (ceFile != null && VfsUtil.isAncestor(ceFile, fileOrDir, false)) return ce;
        }
        // Fallback: add parent as content root if outside existing roots
        var base = fileOrDir.isDirectory() ? fileOrDir : fileOrDir.getParent();
        return model.addContentEntry(base);
    }

    private static String dontAskKey(@NotNull Module module) {
        return DONT_ASK_KEY_PREFIX + module.getName();
    }
}
