package zielu.gittoolbox.ui.projectview;

import git4idea.branch.GitBranchUtil;
import git4idea.repo.GitRepository;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.cache.RepoInfo;
import zielu.gittoolbox.status.Status;
import zielu.gittoolbox.ui.StatusPresenter;

public abstract class NodeDecorationBase implements NodeDecoration {
  protected final NodeDecorationUi ui;
  protected final GitRepository repo;
  protected final RepoInfo repoInfo;

  public NodeDecorationBase(@NotNull NodeDecorationUi ui,
                            @NotNull GitRepository repo,
                            @NotNull RepoInfo repoInfo) {
    this.ui = ui;
    this.repo = repo;
    this.repoInfo = repoInfo;
  }

  @Nullable
  protected final String getCountText() {
    return repoInfo.count().filter(count -> count.status() == Status.SUCCESS).map(count -> {
      StatusPresenter presenter = ui.getPresenter();
      return presenter.nonZeroAheadBehindStatus(count.ahead.value(), count.behind.value());
    }).filter(StringUtils::isNotBlank).orElse(null);
  }

  @NotNull
  protected final String getBranchText() {
    return GitBranchUtil.getDisplayableBranchText(repo);
  }

  @Nullable
  protected final String getTagsText() {
    if (repoInfo.tags().isEmpty()) {
      return null;
    } else {
      return String.join(", ", repoInfo.tags());
    }
  }

  protected final boolean isTrackingBranch() {
    return repoInfo.status().hasRemoteBranch();
  }
}
