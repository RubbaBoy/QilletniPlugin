package dev.qilletni.intellij.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import dev.qilletni.intellij.ui.SelectSpotifyDialog;
import dev.qilletni.intellij.util.QilletniMusicPsiUtil;

public class QilletniSelectSpotifyAction extends AnAction implements DumbAware {
    public QilletniSelectSpotifyAction() { super("Qilletni: Select Spotify music"); }

    @Override
    public void update(AnActionEvent e) {
        var project = e.getProject();
        boolean enabled = false;
        if (project != null) {
            Editor editor = e.getData(CommonDataKeys.EDITOR);
            if (editor != null) {
                var file = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
                if (file != null) {
                    var element = file.findElementAt(editor.getCaretModel().getOffset());
                    enabled = element != null && QilletniMusicPsiUtil.detectContext(element).isPresent();
                }
            }
        }
        e.getPresentation().setEnabledAndVisible(enabled);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        var project = e.getProject();
        if (project == null) return;
        var editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) return;
        var file = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
        if (file == null) return;
        var element = file.findElementAt(editor.getCaretModel().getOffset());
        if (element == null) return;
        var ctxOpt = QilletniMusicPsiUtil.detectContext(element);
        if (ctxOpt.isEmpty()) return;
        var ctx = ctxOpt.get();
        var dialog = new SelectSpotifyDialog(project, ctx.type);
        if (dialog.showAndGet()) {
            dialog.getSelection().ifPresent(choice ->
                    dev.qilletni.intellij.util.QilletniMusicPsiUtil.applySelection(project, editor, ctx, choice,
                            ctx.type == dev.qilletni.intellij.spotify.QilletniSpotifyService.MusicType.SONG && dialog.includeKeyword())
            );
        }
    }
}
