package dev.qilletni.intellij.psi.leaf;

import com.intellij.psi.ContributedReferenceHost;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceService;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Custom leaf for identifier tokens that can host contributed references.
 *
 * This mirrors the pattern from CLIPSPluginScratch: the leaf implements
 * ContributedReferenceHost and delegates reference retrieval to PsiReferenceService,
 * ensuring all registered providers are considered for this element.
 */
public final class QilletniIdentifierLeaf extends LeafPsiElement implements ContributedReferenceHost {
    public QilletniIdentifierLeaf(@NotNull IElementType type, @NotNull CharSequence text) {
        super(type, text);
    }

    @Override
    public PsiReference @NotNull [] getReferences() {
        var service = PsiReferenceService.getService();
        var list = service.getReferences(this, PsiReferenceService.Hints.NO_HINTS);
        return list.toArray(PsiReference.EMPTY_ARRAY);
    }

    @Override
    public @Nullable PsiReference getReference() {
        var refs = getReferences();
        return refs.length > 0 ? refs[0] : null;
    }
}
