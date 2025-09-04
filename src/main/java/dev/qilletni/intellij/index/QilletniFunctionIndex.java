package dev.qilletni.intellij.index;

import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import dev.qilletni.intellij.psi.QilletniFunctionDef;
import org.jetbrains.annotations.NotNull;

public final class QilletniFunctionIndex extends StringStubIndexExtension<QilletniFunctionDef> {
    public static final StubIndexKey<String, QilletniFunctionDef> KEY = StubIndexKey.createIndexKey(QilletniIndexConstants.FUNCTION_INDEX_NAME);

    @Override
    public int getVersion() {
        return QilletniIndexConstants.FUNCTION_INDEX_VERSION;
    }

    @Override
    public @NotNull StubIndexKey<String, QilletniFunctionDef> getKey() {
        return KEY;
    }
}