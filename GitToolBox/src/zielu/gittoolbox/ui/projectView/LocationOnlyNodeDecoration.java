package zielu.gittoolbox.ui.projectView;

import com.google.common.base.Optional;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import git4idea.branch.GitBranchUtil;
import git4idea.repo.GitRepository;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.GitToolBoxConfig;
import zielu.gittoolbox.status.GitAheadBehindCount;

public class LocationOnlyNodeDecoration extends NodeDecorationBase {
    private final GitRepository repo;
    private final Optional<GitAheadBehindCount> aheadBehind;

    public LocationOnlyNodeDecoration(@NotNull GitToolBoxConfig config,
                                      @NotNull GitRepository repo,
                                      @NotNull Optional<GitAheadBehindCount> aheadBehind) {
        super(config);
        this.repo = repo;
        this.aheadBehind = aheadBehind;
    }

    @Override
    public boolean apply(ProjectViewNode node, PresentationData data) {
        data.setLocationString(makeStatusLocation(data.getLocationString(), repo, aheadBehind));
        return true;
    }

    private String makeStatusLocation(String existingLocation, GitRepository repo,
                                  Optional<GitAheadBehindCount> aheadBehind) {
        String locationPath = null;
        if (config.showProjectViewLocationPath && StringUtils.isNotBlank(existingLocation)) {
            locationPath = existingLocation;
        }
        String branch = GitBranchUtil.getDisplayableBranchText(repo);
        String count = getCountText(aheadBehind);
        StringBuilder status = new StringBuilder(branch);
        if (count != null) {
            status.append(" ").append(count);
        }
        StringBuilder location = new StringBuilder();
        if (config.showProjectViewStatusBeforeLocation) {
            location.append(status.toString());
            if (locationPath != null) {
                location.append(" - ").append(locationPath);
            }
        } else {
            if (locationPath != null) {
                location.append(locationPath).append(" - ").append(status.toString());
            } else {
                location.append(status.toString());
            }
        }
        return location.toString();
    }
}
