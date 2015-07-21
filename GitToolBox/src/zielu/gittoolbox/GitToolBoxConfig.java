package zielu.gittoolbox;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import org.jetbrains.annotations.Nullable;

@State(
    name = "GitToolBoxAppSettings",
    storages = {
        @Storage(
            file = StoragePathMacros.APP_CONFIG + "/git_toolbox.xml"
        )
    }
)
public class GitToolBoxConfig implements PersistentStateComponent<GitToolBoxConfig> {
    @Nullable
    @Override
    public GitToolBoxConfig getState() {
        throw new Error("Not yet implemented");
    }

    @Override
    public void loadState(GitToolBoxConfig gitToolBoxConfig) {
        throw new Error("Not yet implemented");
    }

    public static GitToolBoxConfig getInstance() {
        return ServiceManager.getService(GitToolBoxConfig.class);
    }
}
