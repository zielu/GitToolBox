package zielu.gittoolbox.ui.projectview;

import git4idea.branch.GitBranchUtil;
import git4idea.repo.GitRepository;
import java.util.List;
import jodd.util.StringBand;
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
  private String getCountText() {
    return repoInfo.count().filter(count -> count.status() == Status.SUCCESS).map(count -> {
      StatusPresenter presenter = ui.getPresenter();
      return presenter.nonZeroAheadBehindStatus(count.ahead.value(), count.behind.value());
    }).filter(StringUtils::isNotBlank).orElse(null);
  }

  @NotNull
  private String getBranchText() {
    return GitBranchUtil.getDisplayableBranchText(repo);
  }

  @NotNull
  protected final StringBand getStatusText() {
    String branch = getBranchText();
    String count = getCountText();
    StringBand status = new StringBand(branch);
    if (count != null) {
      status.append(" ").append(count);
    }
    List<String> tags = repoInfo.tags();
    if (!tags.isEmpty()) {
      status.append(" (").append(String.join(", ", tags)).append(")");
    }
    return status;
  }

  protected final boolean isTrackingBranch() {
    return repoInfo.status().hasRemoteBranch();
  }
}
