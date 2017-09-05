package zielu.gittoolbox.ui.projectView;

import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.config.GitToolBoxConfig;
import zielu.gittoolbox.status.GitAheadBehindCount;

public class NodeDecorationFactory {
    private static NodeDecorationFactory instance = new NodeDecorationFactory();

    private NodeDecorationFactory() {
    }

    public static NodeDecorationFactory getInstance() {
        return instance;
    }

    public NodeDecoration decorationFor(@NotNull GitRepository repo, @Nullable GitAheadBehindCount aheadBehind) {
        GitToolBoxConfig config = GitToolBoxConfig.getInstance();
        if (config.isProjectViewStatusDecorated()) {
            return new ColoredNodeDecoration(config, repo, aheadBehind);
        } else {
            return new LocationOnlyNodeDecoration(config, repo, aheadBehind);
        }
    }
}
