package zielu.gittoolbox.ui.projectview;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.PresentableNodeDescriptor;
import com.intellij.ui.SimpleTextAttributes;
import git4idea.repo.GitRepository;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import zielu.gittoolbox.cache.RepoInfo;
import zielu.gittoolbox.config.GitToolBoxConfig2;

@Tag("fast")
class ColoredNodeDecorationTest extends NodeDecorationBaseTest {
  @Mock
  private TextAttributesUi attributesUi;

  @BeforeEach
  void setup() {
    when(attributesUi.getTextAttributes(any())).thenReturn(SimpleTextAttributes.REGULAR_ATTRIBUTES);
  }

  @Override
  NodeDecoration createDecoration(GitToolBoxConfig2 config, GitRepository repository, RepoInfo repoInfo) {
    return new ColoredNodeDecoration(new ColoredNodeDecorationUi(config, attributesUi), repository, repoInfo);
  }

  @Override
  DecorationData getDecorationData(PresentationData data) {
    String text = data.getColoredText().stream().map(PresentableNodeDescriptor.ColoredFragment::getText)
        .collect(Collectors.joining(" "));
    return new DecorationData(text, data.getLocationString());
  }

  @Test
  @DisplayName("Name is included if colored text is empty but presentable text is present")
  void nameIsIncludedIfPresentableTextIsPresentAndColoredTextIsEmpty() {
    PresentationData data = presentationData(false);
    final String itemName = "item_name";
    data.setPresentableText(itemName);
    PresentationData presentationData = apply(data);

    DecorationData decorationData = getDecorationData(presentationData);
    assertThat(decorationData.text).startsWith(itemName);
  }
}
