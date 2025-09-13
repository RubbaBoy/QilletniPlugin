package dev.qilletni.intellij.yaml;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.completion.CompletionUtilCore;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AllClassesSearch;
import com.intellij.util.Processor;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLLanguage;
import org.jetbrains.yaml.psi.YAMLScalar;

import java.util.*;

public class QilletniYamlNativeClassCompletionContributor extends CompletionContributor {
    private static final Logger LOG = Logger.getInstance(QilletniYamlNativeClassCompletionContributor.class);
    private static final int MAX_RESULTS = 200;
    private static final int MAX_PACKAGES = 120;

    public QilletniYamlNativeClassCompletionContributor() {
        extend(CompletionType.BASIC,
                com.intellij.patterns.PlatformPatterns.psiElement()
                        .withLanguage(org.jetbrains.yaml.YAMLLanguage.INSTANCE)
                        .withParent(org.jetbrains.yaml.psi.YAMLScalar.class),
                new CompletionProvider<>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters,
                                                  @NotNull ProcessingContext context,
                                                  @NotNull CompletionResultSet result) {
                        LOG.info("[YAML-NativeClasses] addCompletions invoked");
                        var position = parameters.getPosition();
                        var parent = position.getParent();
                        if (!(parent instanceof YAMLScalar scalar)) {
                            LOG.info("[YAML-NativeClasses] Skip: not a YAMLScalar parent");
                            return;
                        }
                        if (!YamlNativeClassesUtil.isInNativeClasses(scalar)) {
                            LOG.info("[YAML-NativeClasses] Skip: not inside native_classes in qilletni_info.yml");
                            return;
                        }
                        var project = position.getProject();
                        if (DumbService.isDumb(project)) {
                            LOG.info("[YAML-NativeClasses] Skip: Dumb mode");
                            return;
                        }

                        // Compute caret-aware prefix within the scalar's value and strip IntelliJ dummy identifier tokens
                        var editor = parameters.getEditor();
                        var doc = editor.getDocument();
                        int caret = parameters.getOffset();
                        var elemRange = scalar.getTextRange();
                        if (caret < elemRange.getStartOffset()) {
                            LOG.info("[YAML-NativeClasses] Skip: caret before element start");
                            return;
                        }
                        String rawPrefix = doc.getText(new TextRange(elemRange.getStartOffset(), Math.min(caret, elemRange.getEndOffset())));
                        // Remove dummy identifiers possibly injected into PSI/document during completion
                        rawPrefix = rawPrefix.replace(CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED, "");
                        rawPrefix = rawPrefix.replace(CompletionUtilCore.DUMMY_IDENTIFIER, "");
                        // If quoted scalar, drop the first quote char
                        if (!rawPrefix.isEmpty()) {
                            char c0 = rawPrefix.charAt(0);
                            if (c0 == '\'' || c0 == '"') {
                                rawPrefix = rawPrefix.substring(1);
                            }
                        }
                        var prefix = rawPrefix.trim();
                        if (prefix.isEmpty()) {
                            LOG.info("[YAML-NativeClasses] Skip: empty prefix (after trimming dummy)");
                            return; // require at least 1 char at the very start
                        }

                        var scope = GlobalSearchScope.projectScope(project);
                        var pfi = ProjectFileIndex.getInstance(project);

                        int lastDot = prefix.lastIndexOf('.');
                        String basePkg = lastDot >= 0 ? prefix.substring(0, lastDot) : "";
                        String namePart = lastDot >= 0 ? prefix.substring(lastDot + 1) : prefix;
                        LOG.info("[YAML-NativeClasses] prefix='" + prefix + "' basePkg='" + basePkg + "' namePart='" + namePart + "'");

                        // Collect classes and packages separately, then emit classes first (higher priority)
                        LinkedHashSet<String> classFqns = new LinkedHashSet<>();
                        LinkedHashSet<String> packageFqns = new LinkedHashSet<>();

                        // Package suggestions
                        if (basePkg.isEmpty()) {
                            LOG.info("[YAML-NativeClasses] Collecting top-level package suggestions...");
                            // Top-level packages whose simple name starts with the prefix
                            PsiPackage rootPkg = JavaPsiFacade.getInstance(project).findPackage("");
                            if (rootPkg != null) {
                                int count = 0;
                                for (PsiPackage sub : rootPkg.getSubPackages(scope)) {
                                    var n = sub.getName();
                                    if (n != null && n.startsWith(prefix) && hasProjectDirs(sub, pfi, scope)) {
                                        var qn = sub.getQualifiedName();
                                        packageFqns.add(qn);
                                        collapseLinearPackage(sub, pfi, scope).ifPresent(packageFqns::add);
                                        if (++count >= MAX_PACKAGES) break;
                                    }
                                }
                            }
                        } else {
                            LOG.info("[YAML-NativeClasses] Collecting subpackages under base='" + basePkg + "' filter='" + namePart + "'");
                            PsiPackage base = JavaPsiFacade.getInstance(project).findPackage(basePkg);
                            if (base != null) {
                                for (PsiPackage sub : base.getSubPackages(scope)) {
                                    var n = sub.getName();
                                    if (n != null && n.startsWith(namePart) && hasProjectDirs(sub, pfi, scope)) {
                                        var qn = sub.getQualifiedName();
                                        packageFqns.add(qn);
                                        collapseLinearPackage(sub, pfi, scope).ifPresent(packageFqns::add);
                                    }
                                    if (packageFqns.size() >= MAX_PACKAGES) break;
                                }
                            } else {
                                LOG.info("[YAML-NativeClasses] Base package not found: " + basePkg);
                            }
                        }

                        // Class suggestions
                        if (basePkg.isEmpty()) {
                            LOG.info("[YAML-NativeClasses] Collecting classes by global prefix match...");
                            // Global scan, filter by FQN prefix
                            AllClassesSearch.search(scope, project).forEach((Processor<PsiClass>) cls -> {
                                var qn = cls.getQualifiedName();
                                if (qn == null || !qn.startsWith(prefix)) return true;
                                var file = cls.getContainingFile();
                                var vf = file != null ? file.getVirtualFile() : null;
                                if (vf == null || !pfi.isInContent(vf)) return true; // strictly project content
                                classFqns.add(qn);
                                return classFqns.size() < MAX_RESULTS;
                            });
                        } else {
                            // Focused within base package
                            LOG.info("[YAML-NativeClasses] Collecting classes inside base='" + basePkg + "' namePart='" + namePart + "'");
                            PsiPackage base = JavaPsiFacade.getInstance(project).findPackage(basePkg);
                            if (base != null) {
                                for (PsiClass cls : base.getClasses(scope)) {
                                    var name = cls.getName();
                                    if (name == null) continue;
                                    if (namePart.isEmpty() || name.startsWith(namePart)) {
                                        var qn = cls.getQualifiedName();
                                        if (qn != null) {
                                            var file = cls.getContainingFile();
                                            var vf = file != null ? file.getVirtualFile() : null;
                                            if (vf != null && pfi.isInContent(vf)) {
                                                classFqns.add(qn);
                                                if (classFqns.size() >= MAX_RESULTS) break;
                                            }
                                        }
                                    }
                                }
                                // If no classes found and namePart not empty, we can still include global matches (fallback)
                                if (classFqns.isEmpty() && !namePart.isEmpty()) {
                                    LOG.info("[YAML-NativeClasses] No classes in base; falling back to global prefix search for '" + prefix + "'");
                                    AllClassesSearch.search(scope, project).forEach((Processor<PsiClass>) cls -> {
                                        var qn = cls.getQualifiedName();
                                        if (qn == null || !qn.startsWith(prefix)) return true;
                                        var file = cls.getContainingFile();
                                        var vf = file != null ? file.getVirtualFile() : null;
                                        if (vf == null || !pfi.isInContent(vf)) return true;
                                        classFqns.add(qn);
                                        return classFqns.size() < MAX_RESULTS;
                                    });
                                }
                            } else {
                                LOG.info("[YAML-NativeClasses] Base package not found for class search: " + basePkg);
                            }
                        }

                        {
                            LOG.info("[YAML-NativeClasses] Found class candidates=" + classFqns.size() + ", package candidates=" + packageFqns.size());
                            LOG.info("[YAML-NativeClasses] Sample classes: " + classFqns.stream().limit(5).toList());
                            LOG.info("[YAML-NativeClasses] Sample packages: " + packageFqns.stream().limit(5).toList());
                        }

                        // Emit results: classes first (higher priority), then packages
                        int emitted = 0;
                        for (String qn : classFqns) {
                            var element = LookupElementBuilder.create(qn)
                                    .withTailText("  (project)", true);
                            // Higher priority for classes
                            result.addElement(PrioritizedLookupElement.withPriority(element, 1000.0));
                            if (++emitted >= MAX_RESULTS) {
                                LOG.info("[YAML-NativeClasses] Stopping after reaching MAX_RESULTS while emitting classes");
                                result.stopHere();
                                return;
                            }
                        }
                        // Suggest packages afterwards, with lower priority
                        for (String pkg : packageFqns) {
                            LookupElement el = LookupElementBuilder.create(pkg)
                                    .withIcon(AllIcons.Nodes.Package)
                                    .withTailText("  (package)", true);
                            result.addElement(PrioritizedLookupElement.withPriority(el, 100.0));
                            if (++emitted >= MAX_RESULTS) {
                                LOG.info("[YAML-NativeClasses] Stopping after reaching MAX_RESULTS while emitting packages");
                                result.stopHere();
                                return;
                            }
                        }

                        LOG.info("[YAML-NativeClasses] Emitted " + emitted + " suggestions for prefix='" + prefix + "'");
                    }
                });
    }

    private static boolean hasProjectDirs(@NotNull PsiPackage pkg,
                                          @NotNull ProjectFileIndex pfi,
                                          @NotNull GlobalSearchScope scope) {
        for (PsiDirectory dir : pkg.getDirectories(scope)) {
            var vf = dir.getVirtualFile();
            if (vf != null && pfi.isInContent(vf)) return true;
        }
        return false;
    }

    /** Walks down a package path while it is a linear chain with no classes, and returns the deepest leaf if deeper than start. */
    private static Optional<String> collapseLinearPackage(@NotNull PsiPackage start,
                                                          @NotNull ProjectFileIndex pfi,
                                                          @NotNull GlobalSearchScope scope) {
        PsiPackage current = start;
        String last = start.getQualifiedName();
        while (true) {
            var subs = current.getSubPackages(scope);
            var classes = current.getClasses(scope);
            if (classes.length > 0) break;
            if (subs.length != 1) break;
            var next = subs[0];
            // ensure next has project content dirs
            if (!hasProjectDirs(next, pfi, scope)) break;
            last = next.getQualifiedName();
            current = next;
        }
        if (!last.equals(start.getQualifiedName())) return Optional.of(last);
        return Optional.empty();
    }

    // Helper to build a YAML scalar PSI pattern without direct dependency here
    private static final class CompletionPatternUtil {
        static com.intellij.patterns.ElementPattern<org.jetbrains.yaml.psi.YAMLScalar> yamlScalarPattern() {
            return com.intellij.patterns.PlatformPatterns.psiElement(YAMLScalar.class).withLanguage(YAMLLanguage.INSTANCE);
        }
    }
}
