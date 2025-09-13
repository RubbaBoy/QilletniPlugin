package dev.qilletni.intellij.library;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Utility for reading native class bindings from qilletni-src/qilletni_info.yml in the current project.
 */
public final class QilletniYamlInfoUtil {
    private static final Logger LOG = Logger.getInstance(QilletniYamlInfoUtil.class);

    private QilletniYamlInfoUtil() {}

    /**
     * If the given file (usually a .ql file) is inside a qilletni-src tree, returns that root.
     */
    public static @Nullable VirtualFile findQilletniSrcRoot(@Nullable VirtualFile vf) {
        VirtualFile cur = vf;
        while (cur != null) {
            if ("qilletni-src".equals(cur.getName())) return cur;
            cur = cur.getParent();
        }
        return null;
    }

    /**
     * Reads native_classes from qilletni_info.yml under the provided qilletni-src root.
     * Returns empty list if file is missing or invalid.
     */
    public static @NotNull List<String> readNativeClassesFromYaml(@NotNull Project project, @NotNull VirtualFile qilletniSrcRoot) {
        if (DumbService.isDumb(project)) return List.of();
        VirtualFile yaml = VfsUtilCore.findRelativeFile("qilletni_info.yml", qilletniSrcRoot);
        if (yaml == null || !yaml.isValid()) return List.of();
        try (InputStream is = yaml.getInputStream(); var reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            var yamlLoader = new Yaml();
            Object data = yamlLoader.load(reader);
            if (!(data instanceof Map<?, ?> map)) return List.of();
            Object nativeClasses = map.get("native_classes");
            if (nativeClasses instanceof List<?> list) {
                return list.stream()
                        .filter(e -> e instanceof String)
                        .map(e -> (String) e)
                        .filter(s -> s != null && !s.isBlank())
                        .toList();
            }
            return List.of();
        } catch (Throwable t) {
            LOG.debug("Failed to parse qilletni_info.yml: " + t.getMessage(), t);
            return Collections.emptyList();
        }
    }
}
