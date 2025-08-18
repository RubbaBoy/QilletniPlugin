package dev.qilletni.intellij;

import com.intellij.lang.ASTFactory;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * ASTFactory for Qilletni. Currently defers to default leaf creation, but is in place for
 * future custom leaf/token handling (e.g., string escapes, identifiers) as needed.
 */
public class QilletniASTFactory extends ASTFactory {
    @Override
    public @Nullable LeafElement createLeaf(@NotNull IElementType type, @NotNull CharSequence text) {
        return super.createLeaf(type, text);
    }

    @Override
    public @Nullable CompositeElement createComposite(@NotNull IElementType type) {
        return super.createComposite(type);
    }
}
