package dev.qilletni.intellij.toolchain;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.util.SimpleModificationTracker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Service(Service.Level.PROJECT)
@State(name = "QilletniToolchainSettingsProject", storages = @Storage("qilletni_toolchain_project.xml"))
public final class QilletniToolchainSettingsProject implements PersistentStateComponent<QilletniToolchainSettingsProject.StateBean> {
    public static final class StateBean { public String toolchainPath; }

    private final Project project;
    private final SimpleModificationTracker modificationTracker = new SimpleModificationTracker();
    private StateBean state = new StateBean();

    public QilletniToolchainSettingsProject(Project project) { this.project = project; }

    public static QilletniToolchainSettingsProject getInstance(Project project) { return project.getService(QilletniToolchainSettingsProject.class); }

    public ModificationTracker getModificationTracker() { return modificationTracker; }

    public void setToolchainPath(@Nullable String path) {
        var newPath = path;
        var old = state.toolchainPath;
        if ((old == null && newPath != null) || (old != null && !old.equals(newPath))) {
            state.toolchainPath = newPath;
            modificationTracker.incModificationCount();
        }
    }

    @Override
    public @Nullable StateBean getState() { return state; }

    @Override
    public void loadState(@NotNull StateBean state) { this.state = state; modificationTracker.incModificationCount(); }
}
