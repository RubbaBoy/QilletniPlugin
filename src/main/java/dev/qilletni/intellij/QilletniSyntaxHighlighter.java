package dev.qilletni.intellij;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.psi.tree.IElementType;
import dev.qilletni.intellij.psi.QilletniTypes;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

/**
 * Syntax highlighter for Qilletni tokens.
 */
public final class QilletniSyntaxHighlighter implements SyntaxHighlighter {
    public static final TextAttributesKey KEYWORD = createTextAttributesKey("QILLETNI_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey TYPE = createTextAttributesKey("QILLETNI_TYPE", DefaultLanguageHighlighterColors.CLASS_NAME);
    public static final TextAttributesKey IDENTIFIER = createTextAttributesKey("QILLETNI_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER);
    public static final TextAttributesKey NUMBER = createTextAttributesKey("QILLETNI_NUMBER", DefaultLanguageHighlighterColors.NUMBER);
    public static final TextAttributesKey STRING = createTextAttributesKey("QILLETNI_STRING", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey LINE_COMMENT = createTextAttributesKey("QILLETNI_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey BLOCK_COMMENT = createTextAttributesKey("QILLETNI_BLOCK_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT);
    public static final TextAttributesKey DOC_COMMENT = createTextAttributesKey("QILLETNI_DOC_COMMENT", DefaultLanguageHighlighterColors.DOC_COMMENT);
    public static final TextAttributesKey OP_SIGN = createTextAttributesKey("QILLETNI_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey PAREN = createTextAttributesKey("QILLETNI_PAREN", DefaultLanguageHighlighterColors.PARENTHESES);
    public static final TextAttributesKey BRACK = createTextAttributesKey("QILLETNI_BRACK", DefaultLanguageHighlighterColors.BRACKETS);
    public static final TextAttributesKey BRACE = createTextAttributesKey("QILLETNI_BRACE", DefaultLanguageHighlighterColors.BRACES);
    public static final TextAttributesKey DOT = createTextAttributesKey("QILLETNI_DOT", DefaultLanguageHighlighterColors.DOT);
    public static final TextAttributesKey COMMA = createTextAttributesKey("QILLETNI_COMMA", DefaultLanguageHighlighterColors.COMMA);
    public static final TextAttributesKey SEMANTIC = createTextAttributesKey("QILLETNI_SEMANTIC", DefaultLanguageHighlighterColors.METADATA);
    public static final TextAttributesKey BAD_CHAR = createTextAttributesKey("QILLETNI_BAD_CHAR", HighlighterColors.BAD_CHARACTER);

    private static final Map<IElementType, TextAttributesKey[]> KEYS = new HashMap<>();

    public static final List<IElementType> NATIVE_TYPES_LIST = List.of(QilletniTypes.ANY_TYPE, QilletniTypes.INT_TYPE, QilletniTypes.DOUBLE_TYPE,
            QilletniTypes.STRING_TYPE, QilletniTypes.BOOLEAN_TYPE, QilletniTypes.COLLECTION_TYPE,
            QilletniTypes.SONG_TYPE, QilletniTypes.ALBUM_TYPE, QilletniTypes.JAVA_TYPE, QilletniTypes.WEIGHTS_KEYWORD);

    static {
        // Types
        putAll(new TextAttributesKey[]{KEYWORD}, NATIVE_TYPES_LIST.toArray(IElementType[]::new));

        // Keywords
        putAll(new TextAttributesKey[]{KEYWORD},
                QilletniTypes.IMPORT, QilletniTypes.AS,
                QilletniTypes.ENTITY, QilletniTypes.NEW,
                QilletniTypes.ORDER_PARAM, QilletniTypes.LIMIT_PARAM, QilletniTypes.LOOP_PARAM,
                QilletniTypes.IS_KEYWORD, QilletniTypes.IF_KEYWORD, QilletniTypes.ELSE_KEYWORD, QilletniTypes.FOR_KEYWORD,
                QilletniTypes.PLAY, QilletniTypes.PROVIDER, QilletniTypes.FUNCTION_DEF, QilletniTypes.STATIC,
                QilletniTypes.NATIVE, QilletniTypes.ON, QilletniTypes.RETURN, QilletniTypes.EMPTY, QilletniTypes.BY);

        // Operators
        putAll(new TextAttributesKey[]{OP_SIGN},
                QilletniTypes.INCREMENT, QilletniTypes.DECREMENT, QilletniTypes.PLUS_EQUALS, QilletniTypes.MINUS_EQUALS,
                QilletniTypes.MINUS, QilletniTypes.PLUS, QilletniTypes.STAR, QilletniTypes.FLOOR_DIV,
                QilletniTypes.DIV, QilletniTypes.MOD, QilletniTypes.ANDAND, QilletniTypes.OROR,
                QilletniTypes.REL_OP, QilletniTypes.ASSIGN, QilletniTypes.NOT, QilletniTypes.DOUBLE_DOT, QilletniTypes.WEIGHT_PIPE);

        // Delimiters
        KEYS.put(QilletniTypes.DOT, arr(DOT));
        KEYS.put(QilletniTypes.COMMA, arr(COMMA));
        KEYS.put(QilletniTypes.LEFT_PAREN, arr(PAREN));
        KEYS.put(QilletniTypes.RIGHT_PAREN, arr(PAREN));
        KEYS.put(QilletniTypes.LEFT_SBRACKET, arr(BRACK));
        KEYS.put(QilletniTypes.RIGHT_SBRACKET, arr(BRACK));
        KEYS.put(QilletniTypes.LEFT_CBRACKET, arr(BRACE));
        KEYS.put(QilletniTypes.RIGHT_CBRACKET, arr(BRACE));

        // Literals
        KEYS.put(QilletniTypes.STRING, arr(STRING));
        putAll(new TextAttributesKey[]{NUMBER}, QilletniTypes.INT, QilletniTypes.DOUBLE);
        putAll(new TextAttributesKey[]{KEYWORD}, QilletniTypes.BOOL, QilletniTypes.RANGE_INFINITY);

        // Comments
        KEYS.put(QilletniTypes.DOC_COMMENT, arr(DOC_COMMENT));
        KEYS.put(QilletniTypes.LINE_COMMENT, arr(LINE_COMMENT));
        KEYS.put(QilletniTypes.BLOCK_COMMENT, arr(BLOCK_COMMENT));

        // Identifiers
        KEYS.put(QilletniTypes.ID, arr(IDENTIFIER));
    }

    private static void putAll(TextAttributesKey[] value, IElementType... keys) {
        for (var k : keys) {
            KEYS.put(k, value);
        }
    }

    private static TextAttributesKey[] arr(TextAttributesKey key) { return new TextAttributesKey[]{key}; }

    @Override
    public @NotNull Lexer getHighlightingLexer() {
        return new QilletniLexerAdapter();
    }

    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        return KEYS.getOrDefault(tokenType, TextAttributesKey.EMPTY_ARRAY);
    }
}
