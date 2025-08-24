package dev.qilletni.intellij.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import dev.qilletni.intellij.QilletniFile;
import dev.qilletni.intellij.resolve.QilletniIndexFacade;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Reference for direct entity type names (e.g., in `new Foo()` or `Foo.bar()`).
 */
public final class QilletniEntityReference extends PsiReferenceBase<PsiElement> {
    public QilletniEntityReference(@NotNull PsiElement element, @NotNull TextRange rangeInElement) {
        super(element, rangeInElement);
    }

    @Override
    public @Nullable PsiElement resolve() {
        var el = getElement();
        var file = el.getContainingFile();
        if (!(file instanceof QilletniFile)) return null;
        var project = el.getProject();
        var name = el.getText();
        return QilletniIndexFacade.findEntityByTypeName(project, (QilletniFile) file, name);
    }

    @Override
    public Object @NotNull [] getVariants() {
        return EMPTY_ARRAY;
    }
}
