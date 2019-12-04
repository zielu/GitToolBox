package zielu.gittoolbox.ui.projectview;

import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.cache.RepoInfo;
import zielu.gittoolbox.changes.ChangesTrackerService;
import zielu.gittoolbox.config.GitToolBoxConfig2;

public class NodeDecorationFactory {
  private static final NodeDecorationFactory INSTANCE = new NodeDecorationFactory();

  private NodeDecorationFactory() {
  }

  public static NodeDecorationFactory getInstance() {
    return INSTANCE;
  }

  public NodeDecoration decorationFor(@NotNull GitRepository repo, @NotNull RepoInfo repoInfo) {
    GitToolBoxConfig2 config = GitToolBoxConfig2.getInstance();
    ColoredNodeDecorationUi ui = new ColoredNodeDecorationUi(config, DecorationColorsTextAttributesUi.getInstance());
    ExtendedRepoInfo extendedInfo;
    if (config.trackChanges) {
      int changesCount = ChangesTrackerService.getInstance(repo.getProject()).getChangesCount(repo);
      extendedInfo = new ExtendedRepoInfo(changesCount);
    } else {
      extendedInfo = new ExtendedRepoInfo();
    }
    return new ColoredNodeDecoration(ui, repo, repoInfo, extendedInfo);
  }
}
