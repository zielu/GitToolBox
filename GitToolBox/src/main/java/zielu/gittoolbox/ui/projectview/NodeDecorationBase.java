package zielu.gittoolbox.ui.projectview;

import com.intellij.dvcs.repo.Repository.State;
import git4idea.GitRemoteBranch;
import git4idea.branch.GitBranchUtil;
import git4idea.repo.GitRepository;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.cache.RepoInfo;
import zielu.gittoolbox.cache.RepoStatus;
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
    if (repo.getState() == State.NORMAL) {
      return getNormalStateBranchText();
    } else if (repo.getState() == State.DETACHED) {
      return getDetachedStateBranchText();
    }
    return GitBranchUtil.getDisplayableBranchText(repo);
  }

  @NotNull
  private String getNormalStateBranchText() {
    RepoStatus status = repoInfo.status();
    if (status.isParentDifferentFromTracking()) {
      GitRemoteBranch parentBranch = status.parentBranch();
      if (parentBranch != null) {
        String branchName = status.localBranch() == null ? "" : status.localBranch().getName();
        String parentBranchName = parentBranch.getNameForRemoteOperations();
        return ui.getPresenter().branchAndParent(branchName, parentBranchName);
      }
    } else if (status.localBranch() != null) {
      return status.localBranch().getName();
    }
    return GitBranchUtil.getDisplayableBranchText(repo);
  }

  @NotNull
  private String getDetachedStateBranchText() {
    RepoStatus status = repoInfo.status();
    return status.localShortHash() == null ? GitBranchUtil.getDisplayableBranchText(repo) : status.localShortHash();
  }

  @Nullable
  protected final String getTagsText() {
    State state = repo.getState();
    if (repoInfo.tags().isEmpty() || (state != State.NORMAL && state != State.DETACHED)) {
      return null;
    } else {
      return String.join(", ", repoInfo.tags());
    }
  }

  protected final boolean isTrackingBranch() {
    return repoInfo.status().isTrackingRemote();
  }
}
