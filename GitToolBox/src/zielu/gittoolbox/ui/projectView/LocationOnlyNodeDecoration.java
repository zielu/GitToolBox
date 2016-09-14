package zielu.gittoolbox.ui.projectView;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import git4idea.repo.GitRepository;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.GitToolBoxConfig;
import zielu.gittoolbox.status.GitAheadBehindCount;

public class LocationOnlyNodeDecoration extends NodeDecorationBase {

    public LocationOnlyNodeDecoration(@NotNull GitToolBoxConfig config,
                                      @NotNull GitRepository repo,
                                      @Nullable GitAheadBehindCount aheadBehind) {
        super(config, repo, aheadBehind);
    }

    @Override
    public boolean apply(ProjectViewNode node, PresentationData data) {
        String initialLocation = data.getLocationString();
        data.setLocationString(makeStatusLocation(initialLocation));
        if (!config.showProjectViewLocationPath && StringUtils.isNotBlank(initialLocation)) {
            data.setTooltip(initialLocation);
        }
        return true;
    }

    private String makeStatusLocation(String existingLocation) {
        String locationPath = null;
        if (config.showProjectViewLocationPath && StringUtils.isNotBlank(existingLocation)) {
            locationPath = existingLocation;
        }
        String status = getStatusText();
        StringBuilder location = new StringBuilder();
        if (config.showProjectViewStatusBeforeLocation) {
            location.append(status);
            if (locationPath != null) {
                location.append(" - ").append(locationPath);
            }
        } else {
            if (locationPath != null) {
                location.append(locationPath).append(" - ").append(status);
            } else {
                location.append(status);
            }
        }
        return location.toString();
    }
}
