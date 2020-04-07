package zielu.gittoolbox.ui.projectview;

import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.cache.RepoInfo;
import zielu.gittoolbox.config.AppConfig;
import zielu.gittoolbox.config.GitToolBoxConfig2;
import zielu.gittoolbox.ui.ExtendedRepoInfo;
import zielu.gittoolbox.ui.ExtendedRepoInfoService;

public class NodeDecorationFactory {
  private static final NodeDecorationFactory INSTANCE = new NodeDecorationFactory();

  private NodeDecorationFactory() {
  }

  public static NodeDecorationFactory getInstance() {
    return INSTANCE;
  }

  public NodeDecoration decorationFor(@NotNull GitRepository repo, @NotNull RepoInfo repoInfo) {
    GitToolBoxConfig2 config = AppConfig.get();
    ColoredNodeDecorationUi ui = new ColoredNodeDecorationUi(config, DecorationColorsTextAttributesUi.getInstance());
    ExtendedRepoInfo extendedInfo = ExtendedRepoInfoService.getInstance().getExtendedRepoInfo(repo);
    return new ColoredNodeDecoration(ui, repo, repoInfo, extendedInfo);
  }
}
