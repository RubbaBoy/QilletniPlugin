package dev.qilletni.intellij.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.TokenType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import dev.qilletni.intellij.QilletniFile;
import dev.qilletni.intellij.psi.QilletniConstructorName;
import dev.qilletni.intellij.psi.QilletniEntityDef;
import dev.qilletni.intellij.psi.QilletniEntityInitialize;
import dev.qilletni.intellij.psi.QilletniEntityName;
import dev.qilletni.intellij.psi.QilletniFunctionCall;
import dev.qilletni.intellij.psi.QilletniFunctionDef;
import dev.qilletni.intellij.psi.QilletniFunctionName;
import dev.qilletni.intellij.psi.QilletniLhsCore;
import dev.qilletni.intellij.psi.QilletniLhsMember;
import dev.qilletni.intellij.psi.QilletniParamName;
import dev.qilletni.intellij.psi.QilletniPostfixExpr;
import dev.qilletni.intellij.psi.QilletniPostfixSuffix;
import dev.qilletni.intellij.psi.QilletniPrimaryExpr;
import dev.qilletni.intellij.psi.QilletniPropertyName;
import dev.qilletni.intellij.psi.QilletniTypes;
import dev.qilletni.intellij.psi.QilletniVarDeclaration;
import dev.qilletni.intellij.psi.QilletniVarName;
import dev.qilletni.intellij.resolve.QilletniIndexFacade;
import dev.qilletni.intellij.resolve.QilletniResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

/**
 * Context-aware code completion for Qilletni:
 * - Entities in 'new Foo()' and static access heads 'Foo.'
 * - Members (properties and methods) in postfix chains and assignment LHS
 * - Top-level functions for bare calls
 */
public final class QilletniCompletionContributor extends CompletionContributor {

    public QilletniCompletionContributor() {
        // Entities in "new Foo()" — be tolerant to intermediate PSI by using super-parent
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement(QilletniTypes.ID).withSuperParent(2, QilletniEntityInitialize.class),
                new EntitiesCompletionProvider());

        // Entities at head of a static call: ID under PrimaryExpr with super PostfixExpr
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement(QilletniTypes.ID)
                        .withParent(QilletniPrimaryExpr.class)
                        .withSuperParent(2, QilletniPostfixExpr.class),
                new EntitiesCompletionProvider());

        // Fallback provider: on any ID, compute context and offer entities/members/functions as appropriate
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement(QilletniTypes.ID),
                new FallbackCompletionProvider());

        // Member properties: ID directly under PostfixSuffix (i.e., `. name`)
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement(QilletniTypes.ID).withParent(QilletniPostfixSuffix.class),
                new MemberPropertiesCompletionProvider());


        // LHS properties: IDs that are direct children of lhs_member
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement(QilletniTypes.ID).withParent(QilletniLhsMember.class),
                new LhsMemberPropertiesCompletionProvider());


    }

    // --- Providers ---

    private static final class EntitiesCompletionProvider extends CompletionProvider<CompletionParameters> {
        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
            System.out.println("========= EntitiesCompletionProvider invoked");
            PsiElement el = parameters.getPosition();
            var file = el.getContainingFile();
            if (!(file instanceof QilletniFile)) {
                return;
            }
            var project = el.getProject();

            var entities = QilletniIndexFacade.listEntitiesInScope(project, (QilletniFile) file);
            for (var e : entities) {
                var nameEl = PsiTreeUtil.findChildOfType(e, QilletniEntityName.class);
                if (nameEl == null) continue;
                    result.addElement(LookupElementBuilder.create(e, nameEl.getText())
                        .withIcon(AllIcons.Nodes.Class));
            }
        }
    }

    private static final class MemberPropertiesCompletionProvider extends CompletionProvider<CompletionParameters> {
        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
            PsiElement idLeaf = parameters.getPosition();
            var file = idLeaf.getContainingFile();
            if (!(file instanceof QilletniFile)) return;

            // Only offer properties if there's a preceding dot or we are under a postfix suffix
            boolean inSuffix = PsiTreeUtil.getParentOfType(idLeaf, QilletniPostfixSuffix.class) != null;
            if (!inSuffix && !hasPrecedingDot(idLeaf)) return;

            var postfix = PsiTreeUtil.getParentOfType(idLeaf, QilletniPostfixExpr.class);
            if (postfix == null) return;
            var primary = PsiTreeUtil.findChildOfType(postfix, QilletniPrimaryExpr.class);

            // Infer receiver type with fallback to variable declaration lookup
            String receiverType = inferReceiverTypeForCompletion((QilletniFile) file, primary);

            // If the base is a type receiver (Foo.), do not propose properties (no static properties yet)
            boolean typeReceiver = false;
            if (receiverType == null) {
                PsiElement headId = findDirectIdChild(primary);
                if (headId != null) {
                    var ent = QilletniIndexFacade.findEntityByTypeName(idLeaf.getProject(), (QilletniFile) file, headId.getText());
                    if (ent != null) {
                        receiverType = headId.getText();
                        typeReceiver = true;
                    }
                }
            }
            if (receiverType == null || typeReceiver) return;

            var entity = QilletniIndexFacade.findEntityByTypeName(idLeaf.getProject(), (QilletniFile) file, receiverType);
            if (entity == null) return;

            for (var p : PsiTreeUtil.findChildrenOfType(entity, QilletniPropertyName.class)) {
                result.addElement(LookupElementBuilder.create(p.getText()));
            }
        }
    }

    private static final class LhsMemberPropertiesCompletionProvider extends CompletionProvider<CompletionParameters> {
        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
            PsiElement idLeaf = parameters.getPosition();
            var file = idLeaf.getContainingFile();
            if (!(file instanceof QilletniFile)) return;

            var lhsMember = PsiTreeUtil.getParentOfType(idLeaf, QilletniLhsMember.class);
            if (lhsMember == null) return;
            var lhsCore = PsiTreeUtil.findChildOfType(lhsMember, QilletniLhsCore.class);
            var primary = lhsCore != null ? PsiTreeUtil.findChildOfType(lhsCore, QilletniPrimaryExpr.class) : null;

            // Infer receiver type with fallback to variable declaration lookup
            String receiverType = inferReceiverTypeForCompletion((QilletniFile) file, primary);
            if (receiverType == null) return;

            var entity = QilletniIndexFacade.findEntityByTypeName(idLeaf.getProject(), (QilletniFile) file, receiverType);
            if (entity == null) return;

            for (var p : PsiTreeUtil.findChildrenOfType(entity, QilletniPropertyName.class)) {
                    result.addElement(LookupElementBuilder.create(p, p.getText())
                        .withIcon(AllIcons.Nodes.ObjectTypeAttribute)
                        .withTypeText(receiverType, true));
            }
        }
    }

    private static final class TopLevelFunctionsCompletionProvider extends CompletionProvider<CompletionParameters> {
        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
            PsiElement idLeaf = parameters.getPosition();
            var file = idLeaf.getContainingFile();
            if (!(file instanceof QilletniFile)) return;
            var project = idLeaf.getProject();

            var functions = QilletniIndexFacade.listTopLevelFunctions(project, (QilletniFile) file);
            for (var f : functions) {
                var nameEl = PsiTreeUtil.findChildOfType(f, QilletniFunctionName.class);
                if (nameEl == null) {
                    continue;
                }
                result.addElement(LookupElementBuilder.create(f, nameEl.getText())
                        .withIcon(AllIcons.Nodes.Method)
                        .withInsertHandler(CALL_PARENS_INSERT_HANDLER));
            }
        }
    }

    // Fallback: invoked for any ID; detect context and dispatch to proper completion sets.
    private static final class FallbackCompletionProvider extends CompletionProvider<CompletionParameters> {
        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
            PsiElement idLeaf = parameters.getPosition();
            var file = idLeaf.getContainingFile();
            if (!(file instanceof QilletniFile)) return;

            // Skip declaration name positions
            var parent = idLeaf.getParent();
            if (parent instanceof QilletniFunctionName
                    || parent instanceof QilletniEntityName
                    || parent instanceof QilletniPropertyName
                    || parent instanceof QilletniConstructorName
                    || parent instanceof QilletniVarName
                    || parent instanceof QilletniParamName) {
                return;
            }

            // Determine context conservatively: treat as member chain only if under a postfix suffix
            boolean inPostfixSuffix = PsiTreeUtil.getParentOfType(idLeaf, QilletniPostfixSuffix.class) != null;
            boolean inLhsMember = PsiTreeUtil.getParentOfType(idLeaf, QilletniLhsMember.class) != null;

            // 1) Postfix member chain: suggest both properties and methods (user may not have typed parentheses yet)
            if (inPostfixSuffix) {
                new MemberPropertiesCompletionProvider().addCompletions(parameters, context, result);
                collectMethodCompletions(idLeaf, result);
                return;
            }

            // 2) LHS assignment member chain (properties only)
            if (inLhsMember) {
                new LhsMemberPropertiesCompletionProvider().addCompletions(parameters, context, result);
                return;
            }

            // 3) Expression head (no chain yet): suggest top-level variables, top-level functions, and entities
            addTopLevelVariableCompletions((QilletniFile) file, result);
            new TopLevelFunctionsCompletionProvider().addCompletions(parameters, context, result);
            new EntitiesCompletionProvider().addCompletions(parameters, context, result);
        }
    }

    // Common utility for method completions (postfix and LHS)
    private static void collectMethodCompletions(PsiElement idLeaf, CompletionResultSet result) {
        var file = idLeaf.getContainingFile();
        if (!(file instanceof QilletniFile)) return;

        // Determine receiver type for method calls:
        // - Postfix: require being under a postfix suffix
        // - LHS: primary_expr in lhs_core head
        PsiElement primary = null;

        boolean inSuffix = PsiTreeUtil.getParentOfType(idLeaf, QilletniPostfixSuffix.class) != null;
        if (!inSuffix) {
            // Not in a proper postfix member; allow LHS methods via lhs_core if needed later
            var lhsCore = PsiTreeUtil.getParentOfType(idLeaf, QilletniLhsCore.class);
            if (lhsCore == null) return;
            primary = PsiTreeUtil.findChildOfType(lhsCore, QilletniPrimaryExpr.class);
        } else {
            var postfix = PsiTreeUtil.getParentOfType(idLeaf, QilletniPostfixExpr.class);
            if (postfix == null) return;
            primary = PsiTreeUtil.findChildOfType(postfix, QilletniPrimaryExpr.class);
        }
        if (primary == null) return;

        // Infer receiver type with fallback to variable declaration lookup
        String receiverType = inferReceiverTypeForCompletion((QilletniFile) file, primary);
        boolean typeReceiver = false;
        if (receiverType == null) {
            PsiElement headId = findDirectIdChild(primary);
            if (headId != null) {
                var ent = QilletniIndexFacade.findEntityByTypeName(idLeaf.getProject(), (QilletniFile) file, headId.getText());
                if (ent != null) {
                    receiverType = headId.getText();
                    typeReceiver = true;
                }
            }
        }
        if (receiverType == null) return;

        var entity = QilletniIndexFacade.findEntityByTypeName(idLeaf.getProject(), (QilletniFile) file, receiverType);
        if (entity != null) {
            for (var def : PsiTreeUtil.findChildrenOfType(entity, QilletniFunctionDef.class)) {
                var nameEl = PsiTreeUtil.findChildOfType(def, QilletniFunctionName.class);
                if (nameEl == null) continue;
                boolean isStatic = isStaticFunctionDef(def);
                if (typeReceiver) {
                    if (!isStatic) continue;
                } else {
                    if (isStatic) continue;
                }
                var functionArgs = def.getFunctionDefParams().getParamNameList().stream().map(QilletniParamName::getText).collect(Collectors.joining(", ", "(", ")"));
                result.addElement(LookupElementBuilder.create(def, nameEl.getText())
                        .bold()
                        .appendTailText(functionArgs, true)
                        .withIcon(AllIcons.Nodes.Method)
                        .withInsertHandler(CALL_PARENS_INSERT_HANDLER));
            }
        }

        // Extension methods for instance receivers only
        if (!typeReceiver) {
            var exts = QilletniIndexFacade.listExtensionMethods(idLeaf.getProject(), (QilletniFile) file, receiverType);
            for (var def : exts) {
                var nameEl = PsiTreeUtil.findChildOfType(def, QilletniFunctionName.class);
                if (nameEl == null) continue;
                var functionArgs = def.getFunctionDefParams().getParamNameList().stream().map(QilletniParamName::getText).collect(Collectors.joining(", ", "(", ")"));
                result.addElement(LookupElementBuilder.create(def, nameEl.getText())
                        .bold()
                        .appendTailText(functionArgs, true)
                        .withIcon(AllIcons.Nodes.MethodReference)
                        .withInsertHandler(CALL_PARENS_INSERT_HANDLER));
            }
        }
    }

    // Small helpers (duplicated minimal logic to avoid depending on private resolve utilities)
    private static PsiElement findDirectIdChild(PsiElement owner) {
        if (owner == null) return null;
        for (PsiElement c = owner.getFirstChild(); c != null; c = c.getNextSibling()) {
            if (c.getNode() != null && c.getNode().getElementType() == QilletniTypes.ID) return c;
        }
        return null;
    }

    private static boolean isStaticFunctionDef(QilletniFunctionDef def) {
        boolean sawStatic = false;
        for (PsiElement c = def.getFirstChild(); c != null; c = c.getNextSibling()) {
            if (c.getNode() == null) continue;
            var t = c.getNode().getElementType();
            if (t == QilletniTypes.STATIC) {
                sawStatic = true;
            }
            if (t == QilletniTypes.FUNCTION_DEF) {
                return sawStatic;
            }
        }
        return sawStatic;
    }

    // Look back for a DOT token, skipping whitespace and comments
    private static boolean hasPrecedingDot(PsiElement leaf) {
        PsiElement prev = com.intellij.psi.util.PsiTreeUtil.prevLeaf(leaf, true);
        while (prev != null) {
            if (prev.getNode() == null) return false;
            var t = prev.getNode().getElementType();
            if (t == TokenType.WHITE_SPACE || t == QilletniTypes.LINE_COMMENT || t == QilletniTypes.BLOCK_COMMENT || t == QilletniTypes.DOC_COMMENT) {
                prev = com.intellij.psi.util.PsiTreeUtil.prevLeaf(prev, true);
                continue;
            }
            return t == QilletniTypes.DOT;
        }
        return false;
    }

    // Infer a receiver type for completion: try static inference, then var-declaration lookup.
    private static String inferReceiverTypeForCompletion(QilletniFile file, PsiElement primary) {
        if (primary == null) return null;
        String t = dev.qilletni.intellij.resolve.QilletniTypeUtil.inferStaticType(primary);
        if (t != null) return t;

        // If primary is a function call, we can't infer without return type info.
        if (PsiTreeUtil.findChildOfType(primary, QilletniFunctionCall.class) != null) return null;

        // Try resolving a variable used as primary and take its declared type.
        PsiElement idLeaf = findDirectIdChild(primary);
        if (idLeaf != null) {
            PsiElement declName = QilletniResolveUtil.resolveVariableUsage(idLeaf);
            if (declName instanceof QilletniVarName) {
                return getVarDeclarationTypeFromVarName((QilletniVarName) declName);
            }
        }
        return null;
    }

    private static String getVarDeclarationTypeFromVarName(QilletniVarName varName) {
        var decl = PsiTreeUtil.getParentOfType(varName, QilletniVarDeclaration.class);
        if (decl == null) return null;
        for (PsiElement c = decl.getFirstChild(); c != null && c != varName; c = c.getNextSibling()) {
            if (c.getNode() == null) continue;
            var tt = c.getNode().getElementType();
            if (tt == QilletniTypes.ID
                    || tt == QilletniTypes.ANY_TYPE || tt == QilletniTypes.INT_TYPE || tt == QilletniTypes.DOUBLE_TYPE
                    || tt == QilletniTypes.STRING_TYPE || tt == QilletniTypes.BOOLEAN_TYPE || tt == QilletniTypes.COLLECTION_TYPE
                    || tt == QilletniTypes.SONG_TYPE || tt == QilletniTypes.ALBUM_TYPE || tt == QilletniTypes.WEIGHTS_KEYWORD
                    || tt == QilletniTypes.JAVA_TYPE) {
                return c.getText();
            }
        }
        return null;
    }

    // Contribute top-level (root-scope) variable names
    private static void addTopLevelVariableCompletions(QilletniFile file, CompletionResultSet result) {
        for (var vName : PsiTreeUtil.findChildrenOfType(file, QilletniVarName.class)) {
            // Only variables not inside any function or entity are considered top-level
            if (PsiTreeUtil.getParentOfType(vName, QilletniFunctionDef.class, false) != null) continue;
            if (PsiTreeUtil.getParentOfType(vName, QilletniEntityDef.class, false) != null) continue;

            String typeText = getVarDeclarationTypeFromVarName(vName);
            var builder = LookupElementBuilder.create(vName.getText())
                    .withIcon(AllIcons.Nodes.Variable)
                    .withTypeText(typeText != null ? typeText : "var", true);
            result.addElement(builder);
        }
    }

    // Insert handler for function/method items: append "()" if not present and put caret between them
    private static final InsertHandler<LookupElement> CALL_PARENS_INSERT_HANDLER = (context, item) -> {
        var doc = context.getDocument();
        int tail = context.getTailOffset();
        CharSequence seq = doc.getCharsSequence();

        // Skip whitespace after the inserted identifier
        int offset = tail;
        while (offset < seq.length() && Character.isWhitespace(seq.charAt(offset))) {
            offset++;
        }

        // If an opening parenthesis is already present right after the identifier (ignoring whitespace), just place caret inside
        if (offset < seq.length() && seq.charAt(offset) == '(') {
            // Move caret just after '('
            context.getEditor().getCaretModel().moveToOffset(offset + 1);
            return;
        }

        // Otherwise insert "()" at the original tail position and move caret inside the parentheses
        doc.insertString(tail, "()");
        context.getEditor().getCaretModel().moveToOffset(tail + 1);
    };
}
