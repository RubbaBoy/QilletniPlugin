package dev.qilletni.intellij.psi.stubs.types;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.io.StringRef;
import dev.qilletni.intellij.index.QilletniIndexConstants;
import dev.qilletni.intellij.psi.*;
import dev.qilletni.intellij.psi.impl.QilletniFunctionDefImpl;
import dev.qilletni.intellij.psi.stubs.QilletniFunctionDefStub;
import dev.qilletni.intellij.psi.stubs.impl.QilletniFunctionDefStubImpl;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public final class QilletniFunctionDefElementType extends IStubElementType<QilletniFunctionDefStub, QilletniFunctionDef> {
    public QilletniFunctionDefElementType() {
        super("FUNCTION_DEF", dev.qilletni.intellij.QilletniLanguage.INSTANCE);
    }

    @Override
    public QilletniFunctionDef createPsi(@NotNull QilletniFunctionDefStub stub) {
        return new QilletniFunctionDefImpl(stub, this);
    }

    @Override
    public @NotNull QilletniFunctionDefStub createStub(@NotNull QilletniFunctionDef psi, StubElement<?> parentStub) {
        String name = psi.getFunctionName().getId().getText();
        boolean isExt = psi.getFunctionOnType() != null;
        String receiver = null;
        if (isExt) {
            System.out.println("[DEBUG_LOG][Stub] createStub for function '" + name + "' isExtension=true");
            // Extract the simple on-type name: take the LAST ID child under functionOnType.
            // This avoids picking the 'on' keyword or qualifiers and matches unqualified types per current scope.
            PsiElement id = null;
            for (PsiElement c = psi.getFunctionOnType().getFirstChild(); c != null; c = c.getNextSibling()) {
                if (c.getNode() != null) {
                    var t = c.getNode().getElementType();
                    if (t == QilletniTypes.ID
                            || t == QilletniTypes.ANY_TYPE || t == QilletniTypes.INT_TYPE || t == QilletniTypes.DOUBLE_TYPE
                            || t == QilletniTypes.STRING_TYPE || t == QilletniTypes.BOOLEAN_TYPE || t == QilletniTypes.COLLECTION_TYPE
                            || t == QilletniTypes.SONG_TYPE || t == QilletniTypes.ALBUM_TYPE || t == QilletniTypes.JAVA_TYPE) {
                        id = c; // keep last seen eligible token
                    }
                }
            }
            receiver = id != null ? id.getText() : null;
        }
        boolean inEntity = PsiTreeUtil.getParentOfType(psi, QilletniEntityDef.class, false) != null;
        return new QilletniFunctionDefStubImpl(parentStub, this, name == null ? "" : name, isExt, receiver == null ? "" : receiver, inEntity);
    }

    @Override
    public @NotNull String getExternalId() {
        return QilletniIndexConstants.FUNCTION_INDEX_NAME + ".functionDef";
    }

    @Override
    public void serialize(@NotNull QilletniFunctionDefStub stub, @NotNull StubOutputStream dataStream) throws IOException {
        if (stub instanceof QilletniFunctionDefStubImpl s) {
            dataStream.writeName(s.getName());
            dataStream.writeBoolean(s.isExtension());
            dataStream.writeName(s.getReceiverSimple());
            dataStream.writeBoolean(s.isInEntity());
        } else {
            dataStream.writeName("");
            dataStream.writeBoolean(false);
            dataStream.writeName("");
            dataStream.writeBoolean(false);
        }
    }

    @Override
    public @NotNull QilletniFunctionDefStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
        String name = StringRef.toString(dataStream.readName());
        boolean isExt = dataStream.readBoolean();
        String recv = StringRef.toString(dataStream.readName());
        boolean inEntity = dataStream.readBoolean();
        return new QilletniFunctionDefStubImpl(parentStub, this, name == null ? "" : name, isExt, recv == null ? "" : recv, inEntity);
    }

    @Override
    public void indexStub(@NotNull QilletniFunctionDefStub stub, @NotNull IndexSink sink) {
        if (!(stub instanceof QilletniFunctionDefStubImpl s)) return;
        String name = s.getName();
        if (name != null && !name.isBlank()) {
            if (!s.isExtension() && !s.isInEntity()) {
                sink.occurrence(StubIndexKey.createIndexKey(QilletniIndexConstants.FUNCTION_INDEX_NAME), name);
            }
            if (s.isExtension()) {
                System.out.println("[DEBUG_LOG][Stub] indexStub ext method name='" + name + "' receiver='" + s.getReceiverSimple() + "'");
                String recv = s.getReceiverSimple();
                String key = dev.qilletni.intellij.index.QilletniIndexConstants.extMethodKey(recv, name);
                if (!key.isBlank()) {
                    sink.occurrence(StubIndexKey.createIndexKey(QilletniIndexConstants.EXT_METHOD_INDEX_NAME), key);
                }
            }
        }
    }
}