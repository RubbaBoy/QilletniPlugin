package dev.qilletni.intellij.resolve.impl;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import dev.qilletni.intellij.resolve.QilletniLibraryPathResolver;

import java.util.List;

/**
 * Default no-op resolver used as a placeholder. It supports no paths and resolves nothing.
 */
public final class NoopLibraryPathResolver implements QilletniLibraryPathResolver {
    @Override
    public boolean supports(String rawImportPath) {
        return false;
    }

    @Override
    public List<VirtualFile> resolve(Project project, String rawImportPath) {
        return List.of();
    }
}