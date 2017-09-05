package zielu.gittoolbox.ui.projectView;

import git4idea.branch.GitBranchUtil;
import git4idea.repo.GitRepository;
import jodd.util.StringBand;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.config.GitToolBoxConfig;
import zielu.gittoolbox.status.GitAheadBehindCount;
import zielu.gittoolbox.status.Status;
import zielu.gittoolbox.ui.StatusPresenter;

public abstract class NodeDecorationBase implements NodeDecoration {
    protected final GitToolBoxConfig config;
    protected final GitRepository repo;
    protected final GitAheadBehindCount aheadBehind;

    public NodeDecorationBase(@NotNull GitToolBoxConfig config,
                              @NotNull GitRepository repo,
                              @Nullable GitAheadBehindCount aheadBehind) {
        this.config = config;
        this.repo = repo;
        this.aheadBehind = aheadBehind;
    }

    @Nullable
    protected final String getCountText() {
        if (aheadBehind != null) {
            if (aheadBehind.status() == Status.Success) {
                StatusPresenter presenter = config.getPresenter();
                String text = presenter.nonZeroAheadBehindStatus(aheadBehind.ahead.value(), aheadBehind.behind.value());
                if (StringUtils.isNotBlank(text)) {
                    return text;
                }
            }
        }
        return null;
    }

    @NotNull
    protected final String getBranchText() {
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
        return status;
    }
}
