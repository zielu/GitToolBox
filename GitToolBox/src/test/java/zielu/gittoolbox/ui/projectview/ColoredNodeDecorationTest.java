package zielu.gittoolbox.ui.projectview;

import static org.assertj.core.api.Assertions.assertThat;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.PresentableNodeDescriptor;
import git4idea.repo.GitRepository;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
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

  @Test
  @DisplayName("Name is included if colored text is empty but presentable text is present")
  void nameIsIncludedIfPresentableTextIsPresentAndColoredTextIsEmpty() {
    PresentationData data = presentationData(false);
    final String itemName = "item_name";
    data.setPresentableText(itemName);
    PresentationData presentationData = apply(data);
    String text = presentationData.getColoredText().stream()
        .map(PresentableNodeDescriptor.ColoredFragment::getText)
        .collect(Collectors.joining());
    assertThat(text).startsWith(itemName);
  }
}
