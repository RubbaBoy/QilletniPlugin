package dev.qilletni.intellij.gutter;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import dev.qilletni.intellij.spotify.QilletniSpotifyService;
import dev.qilletni.intellij.spotify.QilletniSpotifyService.MusicType;
import dev.qilletni.intellij.spotify.QilletniSpotifyService.MusicTypeContext;
import dev.qilletni.intellij.ui.SelectSpotifyDialog;
import dev.qilletni.intellij.util.QilletniMusicPsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QilletniMusicLineMarkerProvider implements LineMarkerProvider {
    @Override
    public @Nullable LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        // Register markers only on leaf STRING tokens to avoid performance warnings
        if (element.getNode() == null || element.getNode().getFirstChildNode() != null) return null;
        var ctxOpt = QilletniMusicPsiUtil.detectContext(element);
        if (ctxOpt.isEmpty()) return null;
        var ctx = ctxOpt.get();
        return new LineMarkerInfo<>(
                element,
                element.getTextRange(),
                AllIcons.Actions.Search,
                psi -> "Select Spotify music…",
                (e, elt) -> {
                    var project = elt.getProject();
                    var dialog = new SelectSpotifyDialog(project, ctx.type());
                    if (dialog.showAndGet()) {
                        var editor = com.intellij.openapi.fileEditor.FileEditorManager.getInstance(project).getSelectedTextEditor();
                        if (editor != null) {
                            dialog.getSelection().ifPresent(choice ->
                                    QilletniMusicPsiUtil.applySelection(project, editor, ctx, choice,
                                            ctx.type() == MusicTypeContext.SONG && dialog.includeKeyword())
                            );
                        }
                    }
                },
                GutterIconRenderer.Alignment.CENTER,
                () -> "Select Spotify music"
        );
    }
}
