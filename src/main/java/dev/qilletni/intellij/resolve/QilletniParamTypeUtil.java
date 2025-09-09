package dev.qilletni.intellij.resolve;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import dev.qilletni.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Parameter typing utilities for Qilletni.
 *
 * Assigns the first parameter of an extension function (with "on <type>") the same type as the receiver.
 * Returns Optional.empty() for all other parameters.
 *
 * Notes for future integrations:
 * - This can be consulted by inspections to validate member access and function calls.
 * - It can be expanded to support richer type systems (nullable, generics) if the language grows.
 */
public final class QilletniParamTypeUtil {
    private QilletniParamTypeUtil() {}

    private static final com.intellij.openapi.util.Key<CachedValue<Optional<String>>> CACHED_ON_TYPE_KEY =
            com.intellij.openapi.util.Key.create("qilletni.function.onType.cached");

    /**
     * If the given param is the first parameter of an extension function, returns the receiver type text.
     * Otherwise returns Optional.empty().
     */
    public static @NotNull Optional<String> getParamType(@NotNull QilletniParamName param) {
        var def = PsiTreeUtil.getParentOfType(param, QilletniFunctionDef.class);
        if (def == null) return Optional.empty();
        var params = def.getFunctionDefParams();
        if (params == null) return Optional.empty();
        // Ensure this is the first parameter
        QilletniParamName first = PsiTreeUtil.findChildOfType(params, QilletniParamName.class);
        if (first == null || first != param) return Optional.empty();
        // Must be an extension function
        if (def.getFunctionOnType() == null) return Optional.empty();
        return getOnTypeCached(def);
    }

    /**
     * Returns the on-type text for an extension function, cached per function node.
     */
    public static @NotNull Optional<String> getOnTypeCached(@NotNull QilletniFunctionDef def) {
        var project = def.getProject();
        CachedValue<Optional<String>> cv = def.getUserData(CACHED_ON_TYPE_KEY);
        if (cv == null) {
            cv = CachedValuesManager.getManager(project).createCachedValue(() ->
                    CachedValueProvider.Result.create(extractOnType(def), com.intellij.psi.util.PsiModificationTracker.MODIFICATION_COUNT), false);
            def.putUserData(CACHED_ON_TYPE_KEY, cv);
        }
        return cv.getValue();
    }

    private static @NotNull Optional<String> extractOnType(@NotNull QilletniFunctionDef def) {
        QilletniFunctionOnType on = def.getFunctionOnType();
        if (on == null) return Optional.empty();
        String result = null;
        for (PsiElement c = on.getFirstChild(); c != null; c = c.getNextSibling()) {
            if (c.getNode() == null) continue;
            var t = c.getNode().getElementType();
            if (t == QilletniTypes.ID
                    || t == QilletniTypes.ANY_TYPE || t == QilletniTypes.INT_TYPE || t == QilletniTypes.DOUBLE_TYPE
                    || t == QilletniTypes.STRING_TYPE || t == QilletniTypes.BOOLEAN_TYPE || t == QilletniTypes.COLLECTION_TYPE
                    || t == QilletniTypes.SONG_TYPE || t == QilletniTypes.ALBUM_TYPE || t == QilletniTypes.WEIGHTS_KEYWORD
                    || t == QilletniTypes.JAVA_TYPE) {
                result = c.getText(); // keep last seen eligible token
            }
        }
        return Optional.ofNullable(result).filter(s -> !s.isBlank());
    }
}
