package dev.qilletni.intellij.psi.impl.mixin;

import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.IStubElementType;
import dev.qilletni.intellij.psi.QilletniEntityDef;
import dev.qilletni.intellij.psi.stubs.QilletniEntityDefStub;
import org.jetbrains.annotations.NotNull;

public abstract class QilletniEntityDefMixin extends StubBasedPsiElementBase<QilletniEntityDefStub> implements QilletniEntityDef {
    public QilletniEntityDefMixin(@NotNull ASTNode node) {
        super(node);
    }

    public QilletniEntityDefMixin(@NotNull QilletniEntityDefStub stub, @NotNull IStubElementType<?, ?> nodeType) {
        super(stub, nodeType);
    }
}
