package dev.qilletni.intellij.resolve.impl;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.DumbService;
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
import dev.qilletni.intellij.library.QilletniYamlInfoUtil;
import dev.qilletni.intellij.library.model.InstalledQllLibrary;
import dev.qilletni.intellij.QilletniFile;
import dev.qilletni.intellij.psi.QilletniFunctionDef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Resolves Qilletni native function definitions to Java PsiMethods.
 * - For current project sources: uses classes listed in qilletni-src/qilletni_info.yml (native_classes) and searches only project sources.
 * - For installed external libraries: falls back to native.jar using nativeClasses from qll.info.
 * Matching is by method name only.
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

        // Determine the method name early; if absent, nothing to resolve.
        var fnNameElt = def.getFunctionName();
        var methodName = fnNameElt == null ? null : fnNameElt.getId().getText();
        if (methodName == null || methodName.isBlank()) return List.of();

        // First: if this file belongs to a project-local qilletni-src, resolve strictly against project sources
        var vFile = qFile.getVirtualFile();
        VirtualFile qilletniSrcRoot = QilletniYamlInfoUtil.findQilletniSrcRoot(vFile);
        if (qilletniSrcRoot != null) {
            var classFqns = QilletniYamlInfoUtil.readNativeClassesFromYaml(project, qilletniSrcRoot);
            if (classFqns.isEmpty()) return List.of();
            var scope = GlobalSearchScope.projectScope(project);
            var jpf = JavaPsiFacade.getInstance(project);
            List<PsiMethod> out = new ArrayList<>();
            for (var fqn : classFqns) {
                if (fqn == null || fqn.isBlank()) continue;
                PsiClass cls = jpf.findClass(fqn, scope);
                if (cls == null) continue; // strictly sources; do not fall back to jar/class files
                for (var m : cls.getMethods()) {
                    if (!m.isConstructor() && methodName.equals(m.getName())) {
                        out.add(m);
                    }
                }
            }
            return Collections.unmodifiableList(out);
        }

        // Otherwise: treat as external library content; resolve against native.jar as before
        var lib = findOwningLibraryFor(qFile);
        if (lib == null) return List.of();
        var jarRoot = lib.nativeJarRoot();
        if (jarRoot == null || !jarRoot.isValid()) {
            return List.of();
        }

        var classFqns = lib.nativeClasses();
        if (classFqns == null || classFqns.isEmpty()) return List.of();

        var scope = directoryScopeFor(project, jarRoot);
        var jpf = JavaPsiFacade.getInstance(project);

        List<PsiMethod> out = new ArrayList<>();
        for (var fqn : classFqns) {
            if (fqn == null || fqn.isBlank()) continue;
            PsiClass cls = jpf.findClass(fqn, scope);

            if (cls == null) {
                // Fallback: direct .class lookup under jar root (external libraries only)
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
