package dev.qilletni.intellij.library.model;

import com.intellij.openapi.vfs.VirtualFile;
import dev.qilletni.api.lib.qll.Version;

import java.nio.file.Path;
import java.util.List;

/**
 * Snapshot of an installed Qilletni library discovered from a .qll archive.
 */
public record InstalledQllLibrary(
        String name,
        Version version,
        String author,
        String description,
        Path archivePath,
        VirtualFile srcRoot,
        List<String> autoImportFiles
) {
}
