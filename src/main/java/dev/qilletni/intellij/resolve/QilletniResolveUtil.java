package dev.qilletni.intellij.resolve;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import dev.qilletni.intellij.QilletniFile;
import dev.qilletni.intellij.psi.*;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

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
        System.out.println("[resolveFunctionUsage] enter");
        if (!isIdLeaf(idElement)) {
            System.out.println("[resolveFunctionUsage] not an ID leaf -> null");
            return null;
        }
        PsiFile file = idElement.getContainingFile();
        if (!(file instanceof QilletniFile)) {
            System.out.println("[resolveFunctionUsage] not a QilletniFile: " + file + " -> null");
            return null;
        }
        var name = idElement.getText();
        System.out.println("[resolveFunctionUsage] looking for function: " + name);
        for (var def : PsiTreeUtil.findChildrenOfType(file, QilletniFunctionDef.class)) {
            var onType = PsiTreeUtil.findChildOfType(def, QilletniFunctionOnType.class);
            // Skip extension/receiver-bound methods
            if (onType != null) {
                System.out.println("[resolveFunctionUsage] skipping extension/receiver-bound function");
                continue;
            }
            var fName = PsiTreeUtil.findChildOfType(def, QilletniFunctionName.class);
            System.out.println("[resolveFunctionUsage] candidate function name: " + (fName != null ? fName.getText() : "null"));
            if (fName != null && textEquals(name, fName.getText())) {
                System.out.println("[resolveFunctionUsage] resolved to function: " + fName.getText());
                return fName;
            }
        }
        System.out.println("[resolveFunctionUsage] unresolved -> null");
        return null;
    }

    public static @Nullable PsiElement resolveMemberUsage(PsiElement idElement) {
        System.out.println("[resolveMemberUsage] enter: idElement=" + idElement + ", text=" + (idElement != null ? idElement.getText() : "null"));
        if (!isIdLeaf(idElement)) {
            System.out.println("[resolveMemberUsage] not an ID leaf -> null");
            return null;
        }

        PsiFile file = idElement.getContainingFile();
        System.out.println("[resolveMemberUsage] file=" + file);
        if (!(file instanceof QilletniFile)) {
            System.out.println("[resolveMemberUsage] not a QilletniFile -> null");
            return null;
        }

        // Determine if this ID is a method name (inside a function_call) or a property name (bare ID segment).
        var isMethodCall = PsiTreeUtil.getParentOfType(idElement, QilletniFunctionCall.class, false, QilletniPrimaryExpr.class) != null
                || PsiTreeUtil.getParentOfType(idElement, QilletniFunctionCall.class, false, QilletniLhsCore.class) != null;
        System.out.println("[resolveMemberUsage] isMethodCall=" + isMethodCall);
        boolean isTypeReceiver = false;

        // 1) Try general postfix expression chains: primary_expr (DOT postfix_suffix)*
        String receiverType = null;
        var postfix = PsiTreeUtil.getParentOfType(idElement, QilletniPostfixExpr.class);
        System.out.println("[resolveMemberUsage] postfix=" + postfix);
        if (postfix != null) {
            var primary = PsiTreeUtil.findChildOfType(postfix, QilletniPrimaryExpr.class);
            System.out.println("[resolveMemberUsage] primary=" + primary);

            // Base type from primary (NEW T(...), variable type, literal, etc.) with fallback via variable declaration.
            receiverType = inferReceiverTypeFromPrimary(file, primary);
            System.out.println("[resolveMemberUsage] base inferred type from primary=" + receiverType);

            // If still unknown, check if primary is a direct entity type reference (e.g., Foo.bar())
            if (receiverType == null && file instanceof QilletniFile) {
                PsiElement headId = findDirectIdChild(primary);
                if (headId != null) {
                    var ent = QilletniIndexFacade.findEntityByTypeName(idElement.getProject(), (QilletniFile) file, headId.getText());
                    if (ent != null) {
                        receiverType = headId.getText();
                        isTypeReceiver = true;
                        System.out.println("[resolveMemberUsage] treating primary ID as type reference: " + receiverType);
                    }
                }
            }

            // Walk through postfix_suffix segments left-to-right up to the current one.
            // We only use property segments (ID) to advance the type; function call return types are unknown here.
            java.util.List<PsiElement> orderedSuffixes = new java.util.ArrayList<>();
            for (var ch : postfix.getChildren()) {
                if (ch instanceof QilletniPostfixSuffix) orderedSuffixes.add(ch);
            }

            int currentIndex = -1;
            for (int i = 0; i < orderedSuffixes.size(); i++) {
                if (com.intellij.psi.util.PsiTreeUtil.isAncestor(orderedSuffixes.get(i), idElement, false)) {
                    currentIndex = i;
                    break;
                }
            }
            System.out.println("[resolveMemberUsage] postfix suffix count=" + orderedSuffixes.size() + ", currentIndex=" + currentIndex);

            if (currentIndex == -1) {
                System.out.println("[resolveMemberUsage] current ID not inside any postfix_suffix of the chain");
            } else if (!isTypeReceiver) {
                // Advance receiver type using previous property segments (only when base is an instance)
                for (int i = 0; i < currentIndex && receiverType != null; i++) {
                    var suff = orderedSuffixes.get(i);
                    var call = PsiTreeUtil.findChildOfType(suff, QilletniFunctionCall.class);
                    if (call != null) {
                        System.out.println("[resolveMemberUsage] encountered function_call before current segment; return type unknown, keeping type=" + receiverType);
                        continue; // cannot update type without return type info
                    }
                    // property segment: get the ID name and advance type through entity property type
                    PsiElement idLeaf = null;
                    for (var c : suff.getChildren()) {
                        if (c.getNode() != null && c.getNode().getElementType() == QilletniTypes.ID) { idLeaf = c; break; }
                    }
                    if (idLeaf != null) {
                        var propName = idLeaf.getText();
                        System.out.println("[resolveMemberUsage] advancing via property '" + propName + "' from type=" + receiverType);
                        var ent = (file instanceof QilletniFile)
                                ? QilletniIndexFacade.findEntityByTypeName(idElement.getProject(), (QilletniFile) file, receiverType)
                                : null;
                        receiverType = ent != null ? getEntityPropertyType(ent, propName) : null;
                        System.out.println("[resolveMemberUsage] type after property '" + propName + "' -> " + receiverType);
                    }
                }
            }
        } else {
            // 2) Try assignment LHS chains per grammar: lhs_member ::= lhs_core (DOT ID)+
            var lhsMember = PsiTreeUtil.getParentOfType(idElement, QilletniLhsMember.class);
            System.out.println("[resolveMemberUsage] lhsMember=" + lhsMember);
            if (lhsMember != null) {
                var lhsCore = PsiTreeUtil.findChildOfType(lhsMember, QilletniLhsCore.class);
                System.out.println("[resolveMemberUsage] lhsCore=" + lhsCore);
                PsiElement primary = lhsCore != null ? PsiTreeUtil.findChildOfType(lhsCore, QilletniPrimaryExpr.class) : null;
                System.out.println("[resolveMemberUsage] lhs primary=" + primary);

                receiverType = inferReceiverTypeFromPrimary(file, primary);
                System.out.println("[resolveMemberUsage] base inferred type from lhs_core primary=" + receiverType);

                // Build ordered list of ID segments that come after lhs_core, in source order
                java.util.List<PsiElement> lhsIds = collectLhsMemberIds(lhsMember, lhsCore);
                int idx = -1;
                for (int i = 0; i < lhsIds.size(); i++) {
                    var it = lhsIds.get(i);
                    if (it == idElement || it.getTextRange().equals(idElement.getTextRange())) { idx = i; break; }
                }
                System.out.println("[resolveMemberUsage] lhs_member IDs count=" + lhsIds.size() + ", currentIdx=" + idx);

                for (int i = 0; i < idx && receiverType != null; i++) {
                    var propName = lhsIds.get(i).getText();
                    System.out.println("[resolveMemberUsage] advancing via lhs property '" + propName + "' from type=" + receiverType);
                    var ent = (file instanceof QilletniFile)
                            ? QilletniIndexFacade.findEntityByTypeName(idElement.getProject(), (QilletniFile) file, receiverType)
                            : null;
                    receiverType = ent != null ? getEntityPropertyType(ent, propName) : null;
                    System.out.println("[resolveMemberUsage] type after lhs property '" + propName + "' -> " + receiverType);
                }
            } else {
                // 3) Method name inside lhs_core (e.g., lhs_core . name(args))
                var lhsCore = PsiTreeUtil.getParentOfType(idElement, QilletniLhsCore.class);
                System.out.println("[resolveMemberUsage] lhsCore (for method)=" + lhsCore);
                if (lhsCore != null) {
                    PsiElement primary = PsiTreeUtil.findChildOfType(lhsCore, QilletniPrimaryExpr.class);
                    receiverType = inferReceiverTypeFromPrimary(file, primary);
                    System.out.println("[resolveMemberUsage] base inferred type from lhs_core primary (method case)=" + receiverType);
                    // We don't attempt to advance through prior function calls here due to unknown return types.
                }
            }
        }

        if (receiverType == null) {
            System.out.println("[resolveMemberUsage] cannot determine receiver type -> null");
            return null;
        }

        // Prefer members declared inside the matching entity
        QilletniEntityDef targetEntity = (file instanceof QilletniFile)
                ? QilletniIndexFacade.findEntityByTypeName(idElement.getProject(), (QilletniFile) file, receiverType)
                : null;
        System.out.println("[resolveMemberUsage] targetEntity (by type '" + receiverType + "')=" + targetEntity);
        if (targetEntity != null) {
            if (isMethodCall) {
                // Search function_def inside entity body
                System.out.println("[resolveMemberUsage] scanning entity methods for: " + idElement.getText());
                for (var f : PsiTreeUtil.findChildrenOfType(targetEntity, QilletniFunctionDef.class)) {
                    var fName = PsiTreeUtil.findChildOfType(f, QilletniFunctionName.class);
                    boolean isStatic = isStaticFunctionDef(f);
                    System.out.println("[resolveMemberUsage] entity method candidate: " + (fName != null ? fName.getText() : "null") + " static=" + isStatic);
                    if (fName != null && textEquals(fName.getText(), idElement.getText())) {
                        if (isTypeReceiver && !isStatic) {
                            // looking for static method but candidate is instance — skip
                            continue;
                        }
                        if (!isTypeReceiver && isStatic) {
                            // looking for instance method but candidate is static — skip
                            continue;
                        }
                        System.out.println("[resolveMemberUsage] resolved to entity method: " + fName.getText());
                        return fName;
                    }
                }
            } else {
                // Property access: search entity_property_declaration
                System.out.println("[resolveMemberUsage] scanning entity properties for: " + idElement.getText());
                for (var prop : PsiTreeUtil.findChildrenOfType(targetEntity, QilletniPropertyName.class)) {
                    System.out.println("[resolveMemberUsage] entity property candidate: " + prop.getText());
                    if (textEquals(prop.getText(), idElement.getText())) {
                        System.out.println("[resolveMemberUsage] resolved to entity property: " + prop.getText());
                        return prop;
                    }
                }
            }
        }

        // Consider extension methods: function_def with function_on_type matching receiverType
        if (isMethodCall && !isTypeReceiver) {
            System.out.println("[resolveMemberUsage] scanning extension methods for type: " + receiverType + ", name: " + idElement.getText());
            if (file instanceof QilletniFile) {
                var defs = QilletniIndexFacade.findExtensionMethods(idElement.getProject(), (QilletniFile) file, receiverType, idElement.getText());
                for (var def : defs) {
                    var fName = PsiTreeUtil.findChildOfType(def, QilletniFunctionName.class);
                    if (fName != null) {
                        System.out.println("[resolveMemberUsage] resolved to extension method: " + fName.getText());
                        return fName;
                    }
                }
            }
        }

        System.out.println("[resolveMemberUsage] unresolved -> null");
        return null;
    }

    // Helper: find entity by name within the same file
    private static @Nullable QilletniEntityDef findEntityInFileByName(PsiFile file, String typeName) {
        if (!(file instanceof QilletniFile) || typeName == null) return null;
        for (var e : PsiTreeUtil.findChildrenOfType(file, QilletniEntityDef.class)) {
            var eName = PsiTreeUtil.findChildOfType(e, QilletniEntityName.class);
            if (eName != null && textEquals(typeName, eName.getText())) return e;
        }
        return null;
    }

    // Helper: get declared type text of an entity property (returns null if not found)
    private static @Nullable String getEntityPropertyType(QilletniEntityDef entity, String propertyName) {
        if (entity == null || propertyName == null) return null;
        Map<String, String> map = getOrBuildEntityPropertyTypeMap(entity);
        return map.get(propertyName);
    }

    // Build and cache a map of propertyName -> declaredTypeText for an entity
    private static java.util.Map<String, String> getOrBuildEntityPropertyTypeMap(QilletniEntityDef entity) {
        return com.intellij.psi.util.CachedValuesManager.getCachedValue(entity, () -> {
            java.util.Map<String, String> m = new java.util.HashMap<>();
            for (var decl : com.intellij.psi.util.PsiTreeUtil.findChildrenOfType(entity, QilletniEntityPropertyDeclaration.class)) {
                QilletniPropertyName propName = com.intellij.psi.util.PsiTreeUtil.findChildOfType(decl, QilletniPropertyName.class);
                if (propName == null) continue;
                String typeText = null;
                for (PsiElement c = decl.getFirstChild(); c != null && c != propName; c = c.getNextSibling()) {
                    if (c.getNode() == null) continue;
                    var t = c.getNode().getElementType();
                    if (t == QilletniTypes.ID
                            || t == QilletniTypes.ANY_TYPE || t == QilletniTypes.INT_TYPE || t == QilletniTypes.DOUBLE_TYPE
                            || t == QilletniTypes.STRING_TYPE || t == QilletniTypes.BOOLEAN_TYPE || t == QilletniTypes.COLLECTION_TYPE
                            || t == QilletniTypes.SONG_TYPE || t == QilletniTypes.ALBUM_TYPE || t == QilletniTypes.WEIGHTS_KEYWORD
                            || t == QilletniTypes.JAVA_TYPE) {
                        typeText = c.getText();
                        break;
                    }
                }
                if (typeText != null) {
                    m.put(propName.getText(), typeText);
                }
            }
            return com.intellij.psi.util.CachedValueProvider.Result.create(m, com.intellij.psi.util.PsiModificationTracker.MODIFICATION_COUNT);
        });
    }

        // Helper: infer receiver type from primary_expr with fallback to variable declaration lookup.
        private static @Nullable String inferReceiverTypeFromPrimary(PsiFile file, PsiElement primary) {
            if (primary == null) return null;
            // First try the general type util.
            String t = QilletniTypeUtil.inferStaticType(primary);
            if (t != null) return t;

            // If primary is a function call, we can't infer without return type info.
            if (PsiTreeUtil.findChildOfType(primary, QilletniFunctionCall.class) != null) return null;

            // Try resolving a variable used as primary and take its declared type.
            PsiElement idLeaf = findDirectIdChild(primary);
            if (idLeaf != null) {
                PsiElement declName = resolveVariableUsage(idLeaf);
                if (declName instanceof QilletniVarName) {
                    return getVarDeclarationType((QilletniVarName) declName);
                }
                // Params have no declared types in grammar — cannot infer from them.
            }
            return null;
        }

        // Helper: find a direct child leaf ID under the given element.
        private static @Nullable PsiElement findDirectIdChild(PsiElement owner) {
            if (owner == null) return null;
            for (PsiElement c = owner.getFirstChild(); c != null; c = c.getNextSibling()) {
                if (c.getNode() != null && c.getNode().getElementType() == QilletniTypes.ID) return c;
            }
            return null;
        }

        // Helper: read declared type text from var_declaration that owns the given var_name
        private static @Nullable String getVarDeclarationType(QilletniVarName varName) {
            if (varName == null) return null;
            var decl = PsiTreeUtil.getParentOfType(varName, QilletniVarDeclaration.class);
            if (decl == null) return null;
            // Scan children before the var_name for the type token or ID
            for (PsiElement c = decl.getFirstChild(); c != null && c != varName; c = c.getNextSibling()) {
                if (c.getNode() == null) continue;
                var t = c.getNode().getElementType();
                if (t == QilletniTypes.ID
                        || t == QilletniTypes.ANY_TYPE || t == QilletniTypes.INT_TYPE || t == QilletniTypes.DOUBLE_TYPE
                        || t == QilletniTypes.STRING_TYPE || t == QilletniTypes.BOOLEAN_TYPE || t == QilletniTypes.COLLECTION_TYPE
                        || t == QilletniTypes.SONG_TYPE || t == QilletniTypes.ALBUM_TYPE || t == QilletniTypes.WEIGHTS_KEYWORD
                        || t == QilletniTypes.JAVA_TYPE) {
                    return c.getText();
                }
            }
            return null;
        }

            // Helper: detect whether a function_def is declared static
            private static boolean isStaticFunctionDef(QilletniFunctionDef def) {
                boolean sawStatic = false;
                for (PsiElement c = def.getFirstChild(); c != null; c = c.getNextSibling()) {
                    if (c.getNode() == null) continue;
                    var t = c.getNode().getElementType();
                    if (t == QilletniTypes.STATIC) {
                        sawStatic = true;
                    }
                    if (t == QilletniTypes.FUNCTION_DEF) {
                        // We reached the 'function' keyword; presence of STATIC token before it is decisive
                        return sawStatic;
                    }
                }
                return sawStatic;
            }

        // Helper: Collect IDs in lhs_member that appear after lhs_core, preserving source order
        private static java.util.List<PsiElement> collectLhsMemberIds(QilletniLhsMember lhsMember, QilletniLhsCore lhsCore) {
            java.util.List<PsiElement> ids = new java.util.ArrayList<>();
            if (lhsMember == null) return ids;
            PsiElement cur = (lhsCore != null) ? lhsCore.getNextSibling() : lhsMember.getFirstChild();
            while (cur != null) {
                if (cur.getNode() != null && cur.getNode().getElementType() == QilletniTypes.ID) {
                    ids.add(cur);
                }
                cur = cur.getNextSibling();
            }
            return ids;
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
