package dev.qilletni.intellij.intentions;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import dev.qilletni.intellij.spotify.QilletniSpotifyService;
import dev.qilletni.intellij.spotify.QilletniSpotifyService.MusicChoice;
import dev.qilletni.intellij.spotify.QilletniSpotifyService.MusicType;
import dev.qilletni.intellij.spotify.QilletniSpotifyService.MusicTypeContext;
import dev.qilletni.intellij.ui.SelectSpotifyDialog;
import dev.qilletni.intellij.util.QilletniMusicPsiUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Intention to insert Spotify music at caret (or replace current music expression).
 * Available broadly in Qilletni files via Alt+Enter: shows the Spotify dialog
 * and inserts a grammar-valid pair. For songs, the default omits the "song" keyword.
 */
public final class QilletniInsertMusicIntention implements IntentionAction {
    @Override
    public @NotNull String getText() { return "Insert music…"; }

    @Override
    public @NotNull String getFamilyName() { return "Qilletni"; }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        // Available for any Qilletni file with an editor
        return editor != null && file != null && file.getFileType().getName().equals("Qilletni");
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) {
        if (editor == null) return;
        // If caret is within a music expression, we can detect context to replace; otherwise we will insert at caret
        PsiElement element = file.findElementAt(editor.getCaretModel().getOffset());
        var existingCtx = element != null ? QilletniMusicPsiUtil.detectContext(element) : java.util.Optional.<QilletniMusicPsiUtil.Context>empty();

        // Initial type: SONG; user can change in dialog
        MusicTypeContext initialType = existingCtx.map(QilletniMusicPsiUtil.Context::type).orElse(MusicTypeContext.SONG);
        var dialog = new SelectSpotifyDialog(project, initialType);
        if (!dialog.showAndGet()) return;

        var selectionOpt = dialog.getSelection();
        if (selectionOpt.isEmpty()) return;
        MusicChoice choice = selectionOpt.get();
        boolean includeSongKeyword = dialog.getType() == MusicType.SONG && dialog.includeKeyword();

        if (existingCtx.isPresent()) {
            QilletniMusicPsiUtil.applySelection(project, editor, existingCtx.get(), choice, includeSongKeyword);
            return;
        }

        // No existing context: insert at caret as raw text
        String text = QilletniMusicPsiUtil.buildReplacementText(dialog.getType(), choice, includeSongKeyword);
        var doc = editor.getDocument();
        int caret = editor.getCaretModel().getOffset();
        WriteCommandAction.runWriteCommandAction(project, "Insert Spotify music", null, () -> {
            doc.insertString(caret, text);
            editor.getCaretModel().moveToOffset(caret + text.length());
        });
    }

    @Override
    public boolean startInWriteAction() { return false; }
}
