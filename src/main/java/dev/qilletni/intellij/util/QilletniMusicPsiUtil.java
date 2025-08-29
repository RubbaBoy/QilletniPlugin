package dev.qilletni.intellij.util;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import dev.qilletni.intellij.psi.QilletniTypes;
import dev.qilletni.intellij.spotify.QilletniSpotifyService.MusicChoice;
import dev.qilletni.intellij.spotify.QilletniSpotifyService.MusicType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Utilities for detecting music expression context and applying Spotify selections.
 */
public final class QilletniMusicPsiUtil {

    public static final class Context {
        public final MusicType type;
        public final PsiElement targetNode; // either STRING or *_url_or_name_pair
        public Context(MusicType type, PsiElement targetNode) { this.type = type; this.targetNode = targetNode; }
    }

    private QilletniMusicPsiUtil() {}

    public static Optional<Context> detectContext(@NotNull PsiElement e) {
        PsiElement cur = e;
        while (cur != null && cur.getNode() != null) {
            var type = cur.getNode().getElementType();
            if (type == QilletniTypes.SONG_URL_OR_NAME_PAIR || type == QilletniTypes.SONG_EXPR) {
                PsiElement target = (type == QilletniTypes.SONG_URL_OR_NAME_PAIR) ? cur : findStringChild(cur, e);
                if (target != null) return Optional.of(new Context(MusicType.SONG, target));
            }
            if (type == QilletniTypes.ALBUM_URL_OR_NAME_PAIR || type == QilletniTypes.ALBUM_EXPR) {
                PsiElement target = (type == QilletniTypes.ALBUM_URL_OR_NAME_PAIR) ? cur : findStringChild(cur, e);
                if (target != null) return Optional.of(new Context(MusicType.ALBUM, target));
            }
            if (type == QilletniTypes.COLLECTION_URL_OR_NAME_PAIR || type == QilletniTypes.COLLECTION_EXPR) {
                PsiElement target = (type == QilletniTypes.COLLECTION_URL_OR_NAME_PAIR) ? cur : findStringChild(cur, e);
                if (target != null) return Optional.of(new Context(MusicType.COLLECTION, target));
            }
            cur = cur.getParent();
        }
        return Optional.empty();
    }

    private static @Nullable PsiElement findStringChild(PsiElement exprNode, PsiElement near) {
        // Prefer the STRING under this expression nearest to 'near'
        PsiElement best = null;
        for (var child : exprNode.getChildren()) {
            if (child.getNode() != null && child.getNode().getElementType() == QilletniTypes.STRING) {
                best = child;
                if (near == child) return child;
            }
        }
        return best;
    }

    public static void applySelection(@NotNull Project project,
                                      @NotNull Editor editor,
                                      @NotNull Context ctx,
                                      @NotNull MusicChoice choice,
                                      boolean includeSongKeyword) {
        var type = ctx.type;
        String name = escape(choice.name);
        String primaryArtistOrOwner = null;
        if (type == MusicType.COLLECTION) {
            primaryArtistOrOwner = choice.owner != null ? escape(choice.owner) : "";
        } else {
            if (choice.artists != null && !choice.artists.isEmpty()) {
                primaryArtistOrOwner = escape(choice.artists.getFirst());
            } else {
                primaryArtistOrOwner = "";
            }
        }
        String replacement = switch (type) {
            case SONG -> {
                // "Name" [song] by "Artist"
                String mid = includeSongKeyword ? " song " : " ";
                yield "\"" + name + "\"" + mid + "by \"" + primaryArtistOrOwner + "\"";
            }
            case ALBUM -> "\"" + name + "\" album by \"" + primaryArtistOrOwner + "\"";
            case COLLECTION -> "\"" + name + "\" collection by \"" + primaryArtistOrOwner + "\"";
        };

        var doc = editor.getDocument();

        var range = ctx.targetNode.getTextRange();
        com.intellij.openapi.command.WriteCommandAction.runWriteCommandAction(project, "Apply Spotify Selection", null, () -> {
            doc.replaceString(range.getStartOffset(), range.getEndOffset(), replacement);
            editor.getCaretModel().moveToOffset(range.getStartOffset() + replacement.length());
            editor.getScrollingModel().scrollToCaret(com.intellij.openapi.editor.ScrollType.MAKE_VISIBLE);
        });
    }

    private static String escape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' || c == '"') sb.append('\\');
            sb.append(c);
        }
        return sb.toString();
    }
}
