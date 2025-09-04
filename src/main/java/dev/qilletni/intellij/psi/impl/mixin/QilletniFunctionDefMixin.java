package dev.qilletni.intellij.psi.impl.mixin;

import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.IStubElementType;
import dev.qilletni.intellij.psi.QilletniFunctionDef;
import dev.qilletni.intellij.psi.stubs.QilletniFunctionDefStub;
import org.jetbrains.annotations.NotNull;

public abstract class QilletniFunctionDefMixin extends StubBasedPsiElementBase<QilletniFunctionDefStub> implements QilletniFunctionDef {
    public QilletniFunctionDefMixin(@NotNull ASTNode node) {
        super(node);
    }

    public QilletniFunctionDefMixin(@NotNull QilletniFunctionDefStub stub, @NotNull IStubElementType<?, ?> nodeType) {
        super(stub, nodeType);
    }
}
