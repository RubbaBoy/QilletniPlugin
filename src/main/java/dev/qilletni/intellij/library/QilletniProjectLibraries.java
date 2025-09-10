package dev.qilletni.intellij.library;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Synchronizes IDEA Project Libraries to mirror installed Qilletni libraries for UI visibility under
 * External Libraries. These libraries are read-only mirrors of qilletni-src roots discovered by
 * QilletniLibraryManager. This is purely for presentation and indexing stability.
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
     * Replace previously attached QilletniLib: libraries with the current set from the manager.
     * Runs inside a write action.
     */
    public static void syncProject(@NotNull Project project) {
        if (project.isDisposed()) return;
        var mgr = QilletniLibraryManager.getInstance();
        var installed = mgr.getInstalled();
        WriteAction.run(() -> {
            LibraryTable table = LibraryTablesRegistrar.getInstance().getLibraryTable(project);
            // Remove any previously-attached QilletniLib libraries to avoid duplicates
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

            // Add a library per installed QLL with SOURCES = qilletni-src and attach to all modules
            if (!installed.isEmpty()) {
                List<Library> created = new ArrayList<>();
                LibraryTable.ModifiableModel mod = table.getModifiableModel();
                try {
                    for (var l : installed) {
                        VirtualFile src = l.srcRoot();
                        if (src == null) continue; // metadata-only library; no sources to expose
                        String ver = l.version() != null ? (" " + l.version().getVersionString()) : "";
                        String name = PREFIX + l.name() + ver;
                        Library lib = mod.createLibrary(name);
                        created.add(lib);
                        var model = lib.getModifiableModel();
                        try {
                            model.addRoot(src, com.intellij.openapi.roots.OrderRootType.SOURCES);
                            // Also add as CLASSES to ensure visibility in some IDE views
                            model.addRoot(src, com.intellij.openapi.roots.OrderRootType.CLASSES);
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
