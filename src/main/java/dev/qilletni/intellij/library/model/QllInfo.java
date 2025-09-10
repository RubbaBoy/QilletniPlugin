package dev.qilletni.intellij.library.model;

import com.google.gson.annotations.SerializedName;
import dev.qilletni.api.lib.qll.Version;

import java.util.List;

/**
 * Metadata model for a Qilletni library contained in a .qll archive.
 * Mirrors the qll.info JSON structure.
 *
 * TODO: Import the Qilletni API for this instead
 */
public record QllInfo(
        String name,
        Version version,
        String author,
        String description,
        String sourceUrl,
        List<Dependency> dependencies,
        String providerClass,
        String nativeBindFactoryClass,
        List<String> nativeClasses,
        List<String> autoImportFiles
) {
    /** Minimal shape for dependency entry; expanded later if needed. */
    public record Dependency(
            String name,
            Version version
    ) {}
}
