package dev.qilletni.intellij.psi.stubs.impl;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import dev.qilletni.intellij.psi.QilletniEntityDef;
import dev.qilletni.intellij.psi.stubs.QilletniEntityDefStub;

public final class QilletniEntityDefStubImpl extends StubBase<QilletniEntityDef> implements QilletniEntityDefStub {
    private final String name;

    public QilletniEntityDefStubImpl(StubElement<?> parent, IStubElementType<?, ?> type, String name) {
        super(parent, (IStubElementType) type);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}