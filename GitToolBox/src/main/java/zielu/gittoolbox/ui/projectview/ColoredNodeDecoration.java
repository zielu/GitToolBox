package zielu.gittoolbox.ui.projectview;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.util.treeView.PresentableNodeDescriptor.ColoredFragment;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.FontUtil;
import git4idea.repo.GitRepository;
import java.util.Optional;
import jodd.util.StringBand;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.cache.RepoInfo;
import zielu.gittoolbox.ui.util.PresentationDataUtil;

public class ColoredNodeDecoration extends NodeDecorationBase {
  private ColoredNodeDecorationUi coloredUi;

  public ColoredNodeDecoration(@NotNull ColoredNodeDecorationUi ui,
                               @NotNull GitRepository repo,
                               @NotNull RepoInfo repoInfo) {
    super(ui, repo, repoInfo);
    coloredUi = ui;
  }

  private ColoredFragment makeStatusFragment() {
    StringBand status = getStatusText();
    return new ColoredFragment(status.toString(), getStatusAttributes());
  }

  private SimpleTextAttributes getStatusAttributes() {
    if (isTrackingBranch()) {
      return coloredUi.getRemoteBranchStatusAttributes();
    } else {
      return coloredUi.getLocalBranchStatusAttributes();
    }
  }

  private ColoredFragment getTagsFragment() {
    StringBand text = getTagsText();
    if (text.length() > 0) {
      return new ColoredFragment(text.toString(), coloredUi.getHeadTagsAttributes());
    } else {
      return null;
    }
  }

  private void appendStatus(PresentationData data) {
    data.addText(makeStatusFragment());
    if (ui.showProjectViewHeadTags()) {
      ColoredFragment tags = getTagsFragment();
      if (tags != null) {
        data.addText(" ", SimpleTextAttributes.REGULAR_ATTRIBUTES);
        data.addText(tags);
      }
    }
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
    if (ui.showProjectViewLocationPath()) {
      if (ui.showProjectViewStatusBeforeLocation()) {
        data.addText(PresentationDataUtil.spacer());
        appendStatus(data);
        locationString.ifPresent(location -> data.setLocationString("- " + location));
      } else {
        if (locationString.isPresent()) {
          StringBand location = new StringBand(FontUtil.spaceAndThinSpace());
          location.append(locationString.get());
          location.append(" - ");
          data.addText(location.toString(), getLocationAttributes());
          appendStatus(data);
          data.setLocationString("");
        } else {
          data.addText(PresentationDataUtil.spacer());
          appendStatus(data);
        }
      }
    } else {
      locationString.ifPresent(data::setTooltip);
      data.setLocationString("");
      data.addText(PresentationDataUtil.spacer());
      appendStatus(data);
    }
    return true;
  }
}
