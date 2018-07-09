package zielu.gittoolbox.ui.projectview;

import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.cache.RepoInfo;
import zielu.gittoolbox.config.GitToolBoxConfig;

public class NodeDecorationFactory {
  private static NodeDecorationFactory instance = new NodeDecorationFactory();

  private NodeDecorationFactory() {
  }

  public static NodeDecorationFactory getInstance() {
    return instance;
  }

  public NodeDecoration decorationFor(@NotNull GitRepository repo, @NotNull RepoInfo repoInfo) {
    GitToolBoxConfig config = GitToolBoxConfig.getInstance();
    ColoredNodeDecorationUi ui = new ColoredNodeDecorationUi(config);
    return new ColoredNodeDecoration(ui, repo, repoInfo);
  }
}
