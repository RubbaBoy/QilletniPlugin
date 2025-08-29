package dev.qilletni.intellij.intentions;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import dev.qilletni.intellij.spotify.QilletniSpotifyService.MusicType;
import dev.qilletni.intellij.ui.SelectSpotifyDialog;
import dev.qilletni.intellij.util.QilletniMusicPsiUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class QilletniSelectSpotifyIntention implements IntentionAction {
    @Override
    public @NotNull String getText() {
        return "Select Spotify music…";
    }

    @Override
    public @NotNull String getFamilyName() { return "Qilletni"; }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        if (editor == null || file == null) return false;
        var offset = editor.getCaretModel().getOffset();
        PsiElement element = file.findElementAt(offset);
        return element != null && QilletniMusicPsiUtil.detectContext(element).isPresent();
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) {
        var element = file.findElementAt(editor.getCaretModel().getOffset());
        if (element == null) return;
        var ctxOpt = QilletniMusicPsiUtil.detectContext(element);
        if (ctxOpt.isEmpty()) return;
        var ctx = ctxOpt.get();
        var dialog = new SelectSpotifyDialog(project, ctx.type);
        if (dialog.showAndGet()) {
            dialog.getSelection().ifPresent(choice ->
                QilletniMusicPsiUtil.applySelection(project, editor, ctx, choice,
                        ctx.type == MusicType.SONG && dialog.includeKeyword())
            );
        }
    }

    @Override
    public boolean startInWriteAction() { return false; }
}
