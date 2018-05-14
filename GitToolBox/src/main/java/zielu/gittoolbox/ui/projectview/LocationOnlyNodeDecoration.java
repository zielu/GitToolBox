package zielu.gittoolbox.ui.projectview;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import git4idea.repo.GitRepository;
import jodd.util.StringBand;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.cache.RepoInfo;

@Deprecated
public class LocationOnlyNodeDecoration extends NodeDecorationBase {

  public LocationOnlyNodeDecoration(@NotNull NodeDecorationUi ui,
                                    @NotNull GitRepository repo,
                                    @NotNull RepoInfo repoInfo) {
    super(ui, repo, repoInfo);
  }

  @Override
  public boolean apply(ProjectViewNode node, PresentationData data) {
    String initialLocation = data.getLocationString();
    data.setLocationString(makeStatusLocation(initialLocation));
    if (!ui.showProjectViewLocationPath() && StringUtils.isNotBlank(initialLocation)) {
      data.setTooltip(initialLocation);
    }
    return true;
  }

  private String makeStatusLocation(String existingLocation) {
    String locationPath = null;
    if (ui.showProjectViewLocationPath() && StringUtils.isNotBlank(existingLocation)) {
      locationPath = existingLocation;
    }
    StringBand status = getStatusText();
    StringBand location = new StringBand();
    if (ui.showProjectViewStatusBeforeLocation()) {
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
