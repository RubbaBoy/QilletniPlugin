package dev.qilletni.intellij.resolve;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import dev.qilletni.intellij.QilletniFile;
import dev.qilletni.intellij.psi.*;
import org.jetbrains.annotations.Nullable;

/**
 * Minimal resolve utilities for Qilletni.
 * - Variables: resolve to params or var_declaration within nearest function
 * - Functions: resolve to top-level functions (no receiver) in the file
 * - Members: resolve to entity properties or methods when receiver type is statically known
 */
public final class QilletniResolveUtil {
    private QilletniResolveUtil() {}

    public static @Nullable PsiElement resolveVariableUsage(PsiElement idElement) {
        if (!isIdLeaf(idElement)) return null;
        PsiElement scope = ascendToFunction(idElement);
        if (scope == null) return null;
        // Params first
        for (var pName : PsiTreeUtil.findChildrenOfType(scope, QilletniParamName.class)) {
            if (textEquals(pName.getText(), idElement.getText())) return pName;
        }
        // Typed var declarations (order not enforced yet)
        for (var vName : PsiTreeUtil.findChildrenOfType(scope, QilletniVarName.class)) {
            if (textEquals(vName.getText(), idElement.getText())) return vName;
        }
        return null;
    }

    public static @Nullable PsiElement resolveFunctionUsage(PsiElement idElement) {
        if (!isIdLeaf(idElement)) return null;
        PsiFile file = idElement.getContainingFile();
        if (!(file instanceof QilletniFile)) return null;
        var name = idElement.getText();
        for (var def : PsiTreeUtil.findChildrenOfType(file, QilletniFunctionDef.class)) {
            // Skip extension/receiver-bound methods
            if (PsiTreeUtil.findChildOfType(def, QilletniFunctionOnType.class) != null) continue;
            var fName = PsiTreeUtil.findChildOfType(def, QilletniFunctionName.class);
            if (fName != null && textEquals(name, fName.getText())) return fName;
        }
        return null;
    }

    public static @Nullable PsiElement resolveMemberUsage(PsiElement idElement) {
        if (!isIdLeaf(idElement)) return null;
        // Find receiver: climb to nearest PostfixExpr, take its first PrimaryExpr as the base
        var postfix = PsiTreeUtil.getParentOfType(idElement, QilletniPostfixExpr.class);
        if (postfix == null) return null;
        var primary = PsiTreeUtil.findChildOfType(postfix, QilletniPrimaryExpr.class);
        var typeName = QilletniTypeUtil.inferStaticType(primary);
        if (typeName == null) return null;

        PsiFile file = idElement.getContainingFile();
        if (!(file instanceof QilletniFile)) return null;
        var isMethodCall = PsiTreeUtil.getParentOfType(idElement, QilletniFunctionCall.class, false, QilletniPrimaryExpr.class) != null;

        // Prefer members declared inside the entity definition
        QilletniEntityDef targetEntity = null;
        for (var e : PsiTreeUtil.findChildrenOfType(file, QilletniEntityDef.class)) {
            var eName = PsiTreeUtil.findChildOfType(e, QilletniEntityName.class);
            if (eName != null && textEquals(typeName, eName.getText())) { targetEntity = e; break; }
        }

        if (targetEntity != null) {
            if (isMethodCall) {
                // Search function_def inside entity body
                for (var f : PsiTreeUtil.findChildrenOfType(targetEntity, QilletniFunctionDef.class)) {
                    var fName = PsiTreeUtil.findChildOfType(f, QilletniFunctionName.class);
                    if (fName != null && textEquals(fName.getText(), idElement.getText())) return fName;
                }
            } else {
                // Property access: search entity_property_declaration
                for (var prop : PsiTreeUtil.findChildrenOfType(targetEntity, QilletniPropertyName.class)) {
                    if (textEquals(prop.getText(), idElement.getText())) return prop;
                }
            }
        }

        // Consider extension methods: function_def with function_on_type matching typeName
        if (isMethodCall) {
            for (var def : PsiTreeUtil.findChildrenOfType(file, QilletniFunctionDef.class)) {
                var onType = PsiTreeUtil.findChildOfType(def, QilletniFunctionOnType.class);
                if (onType == null) continue;
                // function_on_type ::= ON ( ... | ID ) — check for ID child text
                var id = PsiTreeUtil.findChildOfType(onType, com.intellij.psi.PsiElement.class);
                if (id != null && id.getNode() != null && id.getNode().getElementType() == QilletniTypes.ID) {
                    if (textEquals(typeName, id.getText())) {
                        var fName = PsiTreeUtil.findChildOfType(def, QilletniFunctionName.class);
                        if (fName != null && textEquals(fName.getText(), idElement.getText())) return fName;
                    }
                }
            }
        }
        return null;
    }

    private static boolean isIdLeaf(PsiElement el) {
        return el.getNode() != null && el.getNode().getElementType() == QilletniTypes.ID;
    }

    private static boolean textEquals(String a, String b) { return a != null && a.contentEquals(b); }

    private static @Nullable PsiElement ascendToFunction(PsiElement element) {
        PsiElement cur = element;
        while (cur != null) {
            if (cur.getNode() != null && cur.getNode().getElementType() == QilletniTypes.FUNCTION_DEFINITION) return cur;
            cur = cur.getParent();
        }
        return null;
    }
}
