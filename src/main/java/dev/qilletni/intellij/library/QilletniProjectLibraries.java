package dev.qilletni.intellij.library;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Synchronizes real Project Libraries to mirror installed Qilletni libraries so that:
 * - native.jar appears under Classes
 * - qilletni-src appears under Sources
 * Libraries are attached to all modules for visibility under External Libraries.
 */
public final class QilletniProjectLibraries {
    private static final String PREFIX = "Qilletni: ";
    // Debounce syncs to avoid rapid repeated write actions due to bursts of events
    private static final com.intellij.util.Alarm ALARM = new com.intellij.util.Alarm(com.intellij.util.Alarm.ThreadToUse.SWING_THREAD);

    private QilletniProjectLibraries() {}

    /** Request a debounced sync of all open projects (runs on EDT after a short delay). */
    public static void requestSyncAllOpenProjects() {
        ALARM.cancelAllRequests();
        ALARM.addRequest(() -> {
            for (Project project : ProjectManager.getInstance().getOpenProjects()) {
                syncProject(project);
            }
        }, 300);
    }

    /** Synchronize all open projects immediately (EDT only). Prefer requestSyncAllOpenProjects(). */
    public static void syncAllOpenProjects() {
        for (Project project : ProjectManager.getInstance().getOpenProjects()) {
            syncProject(project);
        }
    }

    /**
     * Replace previously attached Qilletni: libraries with the current set from the manager.
     * Runs inside a write action.
     */
    public static void syncProject(@NotNull Project project) {
        if (project.isDisposed()) return;
        var mgr = QilletniLibraryManager.getInstance();
        var installed = mgr.getInstalled();
        WriteAction.run(() -> {
            LibraryTable table = LibraryTablesRegistrar.getInstance().getLibraryTable(project);
            // Remove any previously-attached Qilletni libraries to avoid duplicates
            List<Library> toRemove = new ArrayList<>();
            for (Library lib : table.getLibraries()) {
                var name = lib.getName();
                if (name != null && name.startsWith(PREFIX)) {
                    toRemove.add(lib);
                }
            }

            if (!toRemove.isEmpty()) {
                LibraryTable.ModifiableModel mod = table.getModifiableModel();
                try {
                    for (Library lib : toRemove) mod.removeLibrary(lib);
                } finally {
                    mod.commit();
                }
            }

            // Add a library per installed QLL with CLASSES = native.jar (if present), SOURCES = qilletni-src; attach to modules
            if (!installed.isEmpty()) {
                List<Library> created = new ArrayList<>();
                LibraryTable.ModifiableModel mod = table.getModifiableModel();
                try {
                    for (var l : installed) {
                        VirtualFile src = l.srcRoot();
                        VirtualFile classesRoot = l.nativeJarRoot();
                        if ((src == null || !src.isValid()) && (classesRoot == null || !classesRoot.isValid())) continue;
                        String ver = l.version() != null ? (" " + l.version().getVersionString()) : "";
                        String name = PREFIX + l.name() + ver;
                        Library lib = mod.createLibrary(name);
                        created.add(lib);
                        var model = lib.getModifiableModel();
                        try {
                            if (src != null && src.isValid()) {
                                model.addRoot(src, OrderRootType.SOURCES);
                                // Also add sources as CLASSES so they appear in External Libraries alongside native.jar
                                model.addRoot(src, OrderRootType.CLASSES);
                            }
                            if (classesRoot != null && classesRoot.isValid()) {
                                model.addRoot(classesRoot, OrderRootType.CLASSES);
                            }
                        } finally {
                            model.commit();
                        }
                    }
                } finally {
                    mod.commit();
                }

                // Attach created libraries to all modules so they appear under External Libraries
                var modules = com.intellij.openapi.module.ModuleManager.getInstance(project).getModules();
                for (var module : modules) {
                    var m = com.intellij.openapi.roots.ModuleRootManager.getInstance(module).getModifiableModel();
                    try {
                        for (Library lib : created) {
                            m.addLibraryEntry(lib);
                        }
                    } finally {
                        m.commit();
                    }
                }
            }
        });
    }
}
