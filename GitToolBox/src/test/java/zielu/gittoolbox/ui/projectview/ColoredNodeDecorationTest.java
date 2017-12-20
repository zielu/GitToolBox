package zielu.gittoolbox.ui.projectview;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.PresentableNodeDescriptor;
import git4idea.repo.GitRepository;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import zielu.gittoolbox.cache.RepoInfo;
import zielu.gittoolbox.config.GitToolBoxConfig;

@Tag("fast")
class ColoredNodeDecorationTest extends NodeDecorationBaseTest {
  @Override
  NodeDecoration createDecoration(GitToolBoxConfig config, GitRepository repository, RepoInfo repoInfo) {
    return new ColoredNodeDecoration(config, repository, repoInfo);
  }

  @Override
  String getStatusText(PresentationData data) {
    String locationString = data.getLocationString();
    if (locationString == null) {
      locationString = "";
    } else if (StringUtils.isNotBlank(locationString)) {
      locationString = " " + locationString;
    }
    return data.getColoredText().stream().map(PresentableNodeDescriptor.ColoredFragment::getText)
        .collect(Collectors.joining()).substring(2) + locationString;
  }
}
