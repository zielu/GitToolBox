package zielu.gittoolbox.ui.projectview;

import com.intellij.dvcs.repo.Repository.State;
import git4idea.GitRemoteBranch;
import git4idea.branch.GitBranchUtil;
import git4idea.repo.GitRepository;
import java.util.EnumSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.cache.RepoInfo;
import zielu.gittoolbox.cache.RepoStatus;
import zielu.gittoolbox.status.GitAheadBehindCount;
import zielu.gittoolbox.status.Status;
import zielu.gittoolbox.ui.ExtendedRepoInfo;
import zielu.gittoolbox.ui.StatusPresenter;
import zielu.intellij.util.ZResBundle;

public abstract class NodeDecorationBase implements NodeDecoration {
  private static final EnumSet<State> TAGS_VISIBLE_STATES = EnumSet.of(State.NORMAL, State.DETACHED);
  protected final NodeDecorationUi ui;
  protected final GitRepository repo;
  protected final RepoInfo repoInfo;
  protected final ExtendedRepoInfo extendedRepoInfo;

  public NodeDecorationBase(@NotNull NodeDecorationUi ui,
                            @NotNull GitRepository repo,
                            @NotNull RepoInfo repoInfo,
                            @NotNull ExtendedRepoInfo extendedRepoInfo) {
    this.ui = ui;
    this.repo = repo;
    this.repoInfo = repoInfo;
    this.extendedRepoInfo = extendedRepoInfo;
  }

  @Nullable
  protected final String getCountText() {
    GitAheadBehindCount count = repoInfo.getCount();
    if (count != null && count.status() == Status.SUCCESS) {
      StatusPresenter presenter = ui.getPresenter();
      String status = presenter.nonZeroAheadBehindStatus(
          count.getAhead().value().getAsInt(),
          count.getBehind().value().getAsInt()
      );
      return status.length() > 0 ? status : null;
    }
    return null;
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
    RepoStatus status = repoInfo.getStatus();
    if (status.isParentDifferentFromTracking()) {
      if (status.parentBranch() != null) {
        return getParentBranchText(status);
      }
    } else if (status.localBranch() != null) {
      return status.localBranch().getName();
    }
    return GitBranchUtil.getDisplayableBranchText(repo);
  }

  private String getParentBranchText(RepoStatus status) {
    GitRemoteBranch parentBranch = status.parentBranch();
    String branchName = status.localBranch() == null ? "" : status.localBranch().getName();
    GitRemoteBranch remoteBranch = status.remoteBranch();
    String parentBranchName;
    if (remoteBranch != null && !remoteBranch.getRemote().equals(parentBranch.getRemote())) {
      parentBranchName = parentBranch.getNameForLocalOperations();
    } else {
      parentBranchName = parentBranch.getNameForRemoteOperations();
    }
    return ui.getPresenter().branchAndParent(branchName, parentBranchName);
  }

  @NotNull
  private String getDetachedStateBranchText() {
    RepoStatus status = repoInfo.getStatus();
    if (status.localShortHash() == null) {
      if (repo.getCurrentRevision() != null) {
        return GitBranchUtil.getDisplayableBranchText(repo);
      } else {
        return ZResBundle.INSTANCE.na();
      }
    } else {
      return status.localShortHash();
    }
  }

  @Nullable
  protected final String getTagsText() {
    if (repoInfo.tagsNotEmpty() && TAGS_VISIBLE_STATES.contains(repo.getState())) {
      return String.join(", ", repoInfo.getTags());
    }
    return null;
  }

  @Nullable
  protected final String getChangedCountText() {
    return ui.getPresenter().extendedRepoInfo(extendedRepoInfo);
  }

  protected final boolean isTrackingBranch() {
    return repoInfo.getStatus().isTrackingRemote();
  }

  protected final boolean isMaster() {
    return repoInfo.getStatus().isNameMaster();
  }
}
