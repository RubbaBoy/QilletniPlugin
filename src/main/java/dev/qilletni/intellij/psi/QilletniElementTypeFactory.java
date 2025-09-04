package dev.qilletni.intellij.psi;

import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;
import dev.qilletni.intellij.psi.stubs.types.QilletniEntityDefElementType;
import dev.qilletni.intellij.psi.stubs.types.QilletniFunctionDefElementType;

/**
 * ElementType factory used by Grammar-Kit to create PSI element and stub element types.
 * Must expose createElementType(String) and createIElementType(String) static methods.
 */
public final class QilletniElementTypeFactory {
    private QilletniElementTypeFactory() {}

    public static IElementType createElementType(String name) {
        if ("FUNCTION_DEFINITION".equals(name)) {
            return new QilletniFunctionDefElementType();
        }
        if ("ENTITY_DEF".equals(name)) {
            return new QilletniEntityDefElementType();
        }
        return new QilletniElementType(name);
    }

    // IMPORTANT: Grammar-Kit looks for this exact signature
    public static IStubElementType<?, ?> createIElementType(String name) {
        if ("FUNCTION_DEFINITION".equals(name)) {
            return new QilletniFunctionDefElementType();
        }
        if ("ENTITY_DEF".equals(name)) {
            return new QilletniEntityDefElementType();
        }
        return null;
    }
}
