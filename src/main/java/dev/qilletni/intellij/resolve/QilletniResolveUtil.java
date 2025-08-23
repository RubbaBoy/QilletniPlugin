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
        if (!isIdLeaf(idElement)) {
            System.out.println("[resolveVariableUsage] not an ID leaf -> null");
            return null;
        }
        PsiElement function = ascendToFunction(idElement);
        if (function == null) {
            if (isInRoot(idElement)) {
                System.out.println("[resolveVariableUsage] in root, no enclosing function");
                PsiFile file = idElement.getContainingFile();
                if (file instanceof QilletniFile) {
                    var psiElement = resolveTopLevelUses(idElement, file);
                    if (psiElement != null) {
                        System.out.println("[resolveVariableUsage] no top level vars");
                        return psiElement;
                    }
                }
            } else {
                System.out.println("[resolveVariableUsage] not in root, but no enclosing function -> null");
            }

            return null;
        }

        // 1) Within current function: params first, then local var declarations
        for (var pName : PsiTreeUtil.findChildrenOfType(function, QilletniParamName.class)) {
            if (textEquals(pName.getText(), idElement.getText())) {
                System.out.println("[resolveVariableUsage] resolved to function param: " + pName.getText());
                return pName;
            }
        }
        for (var vName : PsiTreeUtil.findChildrenOfType(function, QilletniVarName.class)) {
            if (textEquals(vName.getText(), idElement.getText())) {
                System.out.println("[resolveVariableUsage] resolved to local var: " + vName.getText());
                return vName;
            }
        }

        // 2) If function is directly in the file, also look for top-level variables in the file scope
        PsiFile file = idElement.getContainingFile();
        System.out.println("parent = " + function.getParent() + "  file = " + file);
        if (file instanceof QilletniFile) {
            if (function.getParent() instanceof QilletniRunning) {
                var psiElement = resolveTopLevelUses(idElement, file);
                if (psiElement != null) {
                    return psiElement;
                }
            } else {
                System.out.println("222");
                // 3) If function is inside an entity, treat as potential member usage and check entity members
                var entity = PsiTreeUtil.getParentOfType(function, QilletniEntityDef.class);
                if (entity != null) {
                    for (var propName : PsiTreeUtil.findChildrenOfType(entity, QilletniPropertyName.class)) {
                        if (textEquals(propName.getText(), idElement.getText())) {
                            System.out.println("[resolveVariableUsage] resolved to entity property: " + propName.getText());
                            return propName;
                        }
                    }
                }
            }
        }

        System.out.println("[resolveVariableUsage] unresolved -> null");
        return null;
    }

    public static PsiElement resolveTopLevelUses(PsiElement idElement, PsiFile file) {
        for (var vName : PsiTreeUtil.findChildrenOfType(file, QilletniVarName.class)) {
            System.out.println("vName = " + vName);
            // Only consider top-level declarations (not inside any function/entity)
            if (PsiTreeUtil.getParentOfType(vName, QilletniFunctionDef.class, false) == null
                    && PsiTreeUtil.getParentOfType(vName, QilletniEntityDef.class, false) == null) {
                if (textEquals(vName.getText(), idElement.getText())) {
                    System.out.println("[resolveVariableUsage] resolved to top-level var: " + vName.getText());
                    return vName;
                }
            }
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

    private static boolean isInRoot(PsiElement element) {
        PsiElement cur = element;
        while (cur != null) {
            if (cur.getNode() != null && (cur.getNode().getElementType() == QilletniTypes.FUNCTION_DEFINITION || cur.getNode().getElementType() == QilletniTypes.ENTITY_DEF)) return false;
            if (cur.getNode() != null && cur.getNode().getElementType() == QilletniTypes.RUNNING) return true;
            cur = cur.getParent();
        }
        return true;
    }
}
