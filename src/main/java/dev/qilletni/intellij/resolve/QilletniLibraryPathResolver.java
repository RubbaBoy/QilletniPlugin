package dev.qilletni.intellij.resolve;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.List;

/**
 * Pluggable resolver for library-style import paths (e.g., "libName:path/in/lib.ql").
 * This is an internal hook surface; real providers will be added in a later plan.
 */
public interface QilletniLibraryPathResolver {
    /**
     * @param rawImportPath the raw string inside the import quotes (e.g., libName:path/in/lib.ql)
     * @return true if this resolver can handle the given path
     */
    boolean supports(String rawImportPath);

    /**
     * Resolve the given import path to one or more VirtualFiles.
     * @param project current project
     * @param rawImportPath the raw string path
     * @param contextFile the importing file's VirtualFile (may be null)
     * @return list of resolved files (possibly empty). Never null.
     */
    List<VirtualFile> resolve(Project project, String rawImportPath, VirtualFile contextFile);
}