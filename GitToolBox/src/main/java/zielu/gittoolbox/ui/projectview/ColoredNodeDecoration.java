package zielu.gittoolbox.ui.projectview;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.util.treeView.PresentableNodeDescriptor.ColoredFragment;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.FontUtil;
import git4idea.repo.GitRepository;
import java.util.Optional;
import jodd.util.StringBand;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.cache.RepoInfo;
import zielu.gittoolbox.config.DecorationColors;
import zielu.gittoolbox.config.GitToolBoxConfig;
import zielu.gittoolbox.ui.util.PresentationDataUtil;

public class ColoredNodeDecoration extends NodeDecorationBase {

  public ColoredNodeDecoration(@NotNull GitToolBoxConfig config,
                               @NotNull GitRepository repo,
                               @NotNull RepoInfo repoInfo) {
    super(config, repo, repoInfo);
  }

  private ColoredFragment makeStatusFragment(boolean prefix) {
    EditorColorsScheme scheme = EditorColorsManager.getInstance().getGlobalScheme();
    SimpleTextAttributes attributes = SimpleTextAttributes.fromTextAttributes(
        scheme.getAttributes(DecorationColors.LOCAL_BRANCH_ATTRIBUTES));
    StringBand status = getStatusText();
    if (prefix) {
      String statusTemp = status.toString();
      status.setIndex(0);
      status.append(FontUtil.spaceAndThinSpace()).append(statusTemp);
    }
    return new ColoredFragment(status.toString(), attributes);
  }

  private SimpleTextAttributes getLocationAttributes() {
    return SimpleTextAttributes.GRAY_ATTRIBUTES;
  }

  private void setName(PresentationData data) {
    Optional<String> textValue = PresentationDataUtil.getFirstColoredTextValue(data);
    if (!textValue.isPresent()) {
      Optional.ofNullable(data.getPresentableText())
          .ifPresent(text -> data.addText(text, SimpleTextAttributes.REGULAR_ATTRIBUTES));
    }
  }

  @Override
  public boolean apply(ProjectViewNode node, PresentationData data) {
    setName(data);
    Optional<String> locationString = Optional.ofNullable(data.getLocationString());
    if (config.showProjectViewLocationPath) {
      if (config.showProjectViewStatusBeforeLocation) {
        data.addText(makeStatusFragment(true));
        locationString.ifPresent(l -> data.setLocationString("- " + l));
      } else {
        if (locationString.isPresent()) {
          StringBand location = new StringBand(FontUtil.spaceAndThinSpace());
          location.append(locationString.get());
          location.append(" - ");
          data.addText(location.toString(), getLocationAttributes());
          data.addText(makeStatusFragment(false));
          data.setLocationString("");
        } else {
          data.addText(makeStatusFragment(true));
        }
      }
    } else {
      locationString.ifPresent(data::setTooltip);
      data.setLocationString("");
      data.addText(makeStatusFragment(true));
    }
    return true;
  }
}
