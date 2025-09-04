package dev.qilletni.intellij.psi.stubs.impl;

import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.IStubElementType;
import dev.qilletni.intellij.psi.QilletniFunctionDef;
import dev.qilletni.intellij.psi.stubs.QilletniFunctionDefStub;

public final class QilletniFunctionDefStubImpl extends StubBase<QilletniFunctionDef> implements QilletniFunctionDefStub {
    private final String name;
    private final boolean isExtension;
    private final String receiverSimple;
    private final boolean inEntity;

    public QilletniFunctionDefStubImpl(StubElement<?> parent, IStubElementType<?, ?> type,
                                       String name, boolean isExtension, String receiverSimple, boolean inEntity) {
        super(parent, (IStubElementType) type);
        this.name = name;
        this.isExtension = isExtension;
        this.receiverSimple = receiverSimple;
        this.inEntity = inEntity;
    }

    public String getName() { return name; }
    public boolean isExtension() { return isExtension; }
    public String getReceiverSimple() { return receiverSimple; }
    public boolean isInEntity() { return inEntity; }
}