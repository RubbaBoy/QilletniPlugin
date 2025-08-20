package dev.qilletni.intellij;

import com.intellij.lang.ASTFactory;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.tree.IElementType;
import dev.qilletni.intellij.psi.QilletniTypes;
import dev.qilletni.intellij.psi.leaf.QilletniIdentifierLeaf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * ASTFactory for Qilletni. Ensures identifier leaves are ContributedReferenceHost
 * so PsiReferenceContributor providers can attach references to usages.
 */
public class QilletniASTFactory extends ASTFactory {
    @Override
    public @Nullable LeafElement createLeaf(@NotNull IElementType type, @NotNull CharSequence text) {
        if (type == QilletniTypes.ID) {
            return new QilletniIdentifierLeaf(type, text);
        }
        return null; // delegate to default factory for other token types
    }

    @Override
    public @Nullable CompositeElement createComposite(@NotNull IElementType type) {
        return super.createComposite(type);
    }
}
