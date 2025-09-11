package dev.qilletni.intellij.resolve.impl;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassOwner;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopesCore;
import dev.qilletni.intellij.library.QilletniLibraryManager;
import dev.qilletni.intellij.library.model.InstalledQllLibrary;
import dev.qilletni.intellij.QilletniFile;
import dev.qilletni.intellij.psi.QilletniFunctionDef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Resolves Qilletni native function definitions to Java PsiMethods declared in native.jar
 * according to the owning library's nativeClasses list. Matching is by method name only.
 * TODO: improve matching by signature mapping between Qilletni and JVM types.
 */
public final class QilletniNativeBindingResolver {

    private QilletniNativeBindingResolver() {}

    /** Returns Java methods that match the given native function definition (by name only). */
    public static @NotNull List<PsiMethod> resolveJavaTargets(@NotNull QilletniFunctionDef def) {
        var file = def.getContainingFile();
        if (!(file instanceof QilletniFile qFile)) {
            return List.of();
        }
        var project = qFile.getProject();

        if (DumbService.isDumb(project)) {
            return List.of();
        }

        var lib = findOwningLibraryFor(qFile);
        if (lib == null) return List.of();
        var jarRoot = lib.nativeJarRoot();
        if (jarRoot == null || !jarRoot.isValid()) {
            return List.of();
        }

        // Classify jarRoot in project index
        try {
            var index = ProjectFileIndex.getInstance(project);
        } catch (Throwable t) {
        }

        var classFqns = lib.nativeClasses();
        if (classFqns == null || classFqns.isEmpty()) return List.of();

        var scope = directoryScopeFor(project, jarRoot);
        var jpf = JavaPsiFacade.getInstance(project);
        var fnNameElt = def.getFunctionName();
        var methodName = fnNameElt == null ? null : fnNameElt.getId().getText();
        if (methodName == null || methodName.isBlank()) return List.of();

        List<PsiMethod> out = new ArrayList<>();
        for (var fqn : classFqns) {
            if (fqn == null || fqn.isBlank()) continue;
            PsiClass cls = jpf.findClass(fqn, scope);

            if (cls == null) {
                // Fallback: direct .class lookup under jar root
                var rel = fqn.replace('.', '/') + ".class";
                VirtualFile classVf = VfsUtilCore.findRelativeFile(rel, jarRoot);
                if (classVf != null) {
                    PsiFile psiFile = PsiManager.getInstance(project).findFile(classVf);
                    if (psiFile instanceof PsiClassOwner owner) {
                        var classes = owner.getClasses();
                        for (var c : classes) {
                            var qn = c.getQualifiedName();
                            if (qn != null && qn.equals(fqn)) {
                                cls = c; // use this class for method match below
                                break;
                            }
                        }
                    }
                }
            }

            if (cls == null) {
                continue;
            }

            for (var m : cls.getMethods()) {
                if (!m.isConstructor() && methodName.equals(m.getName())) {
                    out.add(m);
                }
            }
        }
        return Collections.unmodifiableList(out);
    }

    private static @NotNull GlobalSearchScope directoryScopeFor(@NotNull Project project, @NotNull VirtualFile root) {
        // For jar roots, directoryScope works and includes entries recursively.
        return GlobalSearchScopesCore.directoryScope(project, root, true);
    }

    /** Attempts to locate the InstalledQllLibrary for the given Qilletni file based on its source root. */
    private static @Nullable InstalledQllLibrary findOwningLibraryFor(@NotNull QilletniFile file) {
        var vFile = file.getVirtualFile();
        if (vFile == null) return null;
        for (var lib : QilletniLibraryManager.getInstance().getInstalled()) {
            var src = lib.srcRoot();
            if (src != null && src.isValid() && VfsUtilCore.isAncestor(src, vFile, false)) {
                return lib;
            }
        }
        return null;
    }
}
