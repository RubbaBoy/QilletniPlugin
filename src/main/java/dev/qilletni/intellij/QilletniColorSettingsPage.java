package dev.qilletni.intellij;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

/**
 * Color settings for Qilletni highlighting.
 */
public final class QilletniColorSettingsPage implements ColorSettingsPage {
    private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
            new AttributesDescriptor("Keyword", QilletniSyntaxHighlighter.KEYWORD),
            new AttributesDescriptor("Type", QilletniSyntaxHighlighter.TYPE),
            new AttributesDescriptor("Identifier", QilletniSyntaxHighlighter.IDENTIFIER),
            new AttributesDescriptor("Number", QilletniSyntaxHighlighter.NUMBER),
            new AttributesDescriptor("String", QilletniSyntaxHighlighter.STRING),
            new AttributesDescriptor("Line comment", QilletniSyntaxHighlighter.LINE_COMMENT),
            new AttributesDescriptor("Block comment", QilletniSyntaxHighlighter.BLOCK_COMMENT),
            new AttributesDescriptor("Doc comment", QilletniSyntaxHighlighter.DOC_COMMENT),
            new AttributesDescriptor("Operator", QilletniSyntaxHighlighter.OP_SIGN),
            new AttributesDescriptor("Braces", QilletniSyntaxHighlighter.BRACE),
            new AttributesDescriptor("Brackets", QilletniSyntaxHighlighter.BRACK),
            new AttributesDescriptor("Parentheses", QilletniSyntaxHighlighter.PAREN),
            new AttributesDescriptor("Dot", QilletniSyntaxHighlighter.DOT),
            new AttributesDescriptor("Comma", QilletniSyntaxHighlighter.COMMA),
            new AttributesDescriptor("Bad character", QilletniSyntaxHighlighter.BAD_CHAR)
    };

    @Override
    public @Nullable Icon getIcon() {
        return QilletniIcons.FILE;
    }

    @Override
    public @NotNull String getDemoText() {
        return """
                /** Quick demo */
                import "lib/common.ql" as common
                
                provider "spotify" {
                  fun init(on collection) {
                    return empty
                  }
                }
                
                entity Playlist {
                  string name = "My Mix"
                  song[] tracks
                  fun add(id) { tracks[id] = new song(\"id\" by \"artist\") }
                }
                
                fun addAll(list) on collection {
                  list.forEach: [1,2,3]
                  return true
                }
                
                play common.mix limit[30m] loop
                """;
    }

    @Override
    public @Nullable Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return null;
    }

    @Override
    public @NotNull AttributesDescriptor[] getAttributeDescriptors() {
        return DESCRIPTORS;
    }

    @Override
    public @NotNull ColorDescriptor[] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @Override
    public @NotNull String getDisplayName() {
        return "Qilletni";
    }

    @Override
    public @NotNull SyntaxHighlighter getHighlighter() {
        return new QilletniSyntaxHighlighter();
    }
}
