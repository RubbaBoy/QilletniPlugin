package dev.qilletni.intellij.index;

import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import dev.qilletni.intellij.psi.QilletniEntityDef;
import org.jetbrains.annotations.NotNull;

public final class QilletniEntityIndex extends StringStubIndexExtension<QilletniEntityDef> {
    public static final StubIndexKey<String, QilletniEntityDef> KEY = StubIndexKey.createIndexKey(QilletniIndexConstants.ENTITY_INDEX_NAME);

    @Override
    public int getVersion() {
        return QilletniIndexConstants.ENTITY_INDEX_VERSION;
    }

    @Override
    public @NotNull StubIndexKey<String, QilletniEntityDef> getKey() {
        return KEY;
    }
}