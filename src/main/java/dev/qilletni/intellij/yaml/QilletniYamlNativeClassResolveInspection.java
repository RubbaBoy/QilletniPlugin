package dev.qilletni.intellij.yaml;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.DumbService;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReferenceService;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLScalar;
import org.jetbrains.yaml.psi.YamlPsiElementVisitor;

/** Warns when a native_classes entry does not resolve to a project Java class. */
public class QilletniYamlNativeClassResolveInspection extends LocalInspectionTool {

    @Override
    public @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String getGroupDisplayName() {
        return "Qilletni";
    }

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new YamlPsiElementVisitor() {
            @Override
            public void visitScalar(@NotNull YAMLScalar scalar) {
                if (!YamlNativeClassesUtil.isInNativeClasses(scalar)) return;
                var project = scalar.getProject();
                if (DumbService.isDumb(project)) return;
                var refs = PsiReferenceService.getService().getReferences(scalar, PsiReferenceService.Hints.NO_HINTS);
                var hasResolved = false;
                for (var ref : refs) {
                    var target = ref.resolve();
                    if (target != null) { hasResolved = true; break; }
                }
                if (!hasResolved) {
                    holder.registerProblem(scalar, "Class not found in project sources", ProblemHighlightType.LIKE_UNKNOWN_SYMBOL);
                }
            }
        };
    }

    @Override
    public boolean runForWholeFile() {
        return false;
    }
}