package zielu.gittoolbox.ui.projectview;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.util.treeView.PresentableNodeDescriptor.ColoredFragment;
import com.intellij.ui.SimpleTextAttributes;
import git4idea.repo.GitRepository;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.cache.RepoInfo;
import zielu.gittoolbox.config.DecorationPartType;
import zielu.gittoolbox.ui.util.PresentationDataUtil;

public class ColoredNodeDecoration extends NodeDecorationBase {
  private ColoredNodeDecorationUi coloredUi;

  public ColoredNodeDecoration(@NotNull ColoredNodeDecorationUi ui,
                               @NotNull GitRepository repo,
                               @NotNull RepoInfo repoInfo) {
    super(ui, repo, repoInfo);
    coloredUi = ui;
  }

  @Nullable
  private ColoredFragment getFragmentFor(DecorationPartType type) {
    String value = null;
    SimpleTextAttributes attributes = null;
    switch (type) {
      case BRANCH: {
        value = coloredUi.getDecorationPartText(getBranchText(), type);
        attributes = getBranchAttributes();
        break;
      }
      case STATUS: {
        value = coloredUi.getDecorationPartText(getCountText(), type);
        attributes = coloredUi.getStatusAttributes();
        break;
      }
      case TAGS_ON_HEAD: {
        value = coloredUi.getDecorationPartText(getTagsText(), type);
        attributes = coloredUi.getHeadTagsAttributes();
        break;
      }
      default: {
        break;
      }
    }

    if (value != null) {
      return new ColoredFragment(value, attributes);
    } else {
      return null;
    }
  }

  private SimpleTextAttributes getBranchAttributes() {
    if (isTrackingBranch()) {
      return coloredUi.getRemoteBranchAttributes();
    } else {
      return coloredUi.getLocalBranchAttributes();
    }
  }

  private void setName(PresentationData data) {
    Optional<String> textValue = PresentationDataUtil.getFirstColoredTextValue(data);
    if (!textValue.isPresent()) {
      Optional.ofNullable(data.getPresentableText())
          .ifPresent(text -> data.addText(text, coloredUi.getNameAttributes()));
    }
  }

  @Override
  public boolean apply(ProjectViewNode node, PresentationData data) {
    setName(data);
    Optional<String> locationString = Optional.ofNullable(data.getLocationString());
    if (ui.hasLocationPart()) {
      if (ui.isLocationPartLast()) {
        locationString.map(location -> ui.getDecorationPartText(location, DecorationPartType.LOCATION))
            .map(text -> " " + text)
            .ifPresent(data::setLocationString);
      }
    } else {
      locationString.ifPresent(data::setTooltip);
      data.setLocationString("");
    }

    final AtomicBoolean first = new AtomicBoolean(true);
    Collection<DecorationPartType> types = ui.getDecorationTypes();
    for (DecorationPartType type : types) {
      if (type == DecorationPartType.LOCATION && !ui.isLocationPartLast()) {
        locationString.ifPresent(location -> {
          location = " " + coloredUi.getDecorationPartText(location, type);
          data.addText(getSpacer(first));
          data.addText(new ColoredFragment(location, coloredUi.getLocationAttributes()));
          data.setLocationString("");
          first.set(false);
        });
      } else {
        ColoredFragment fragment = getFragmentFor(type);
        if (fragment != null) {
          data.addText(getSpacer(first));
          data.addText(fragment);
          first.set(false);
        }
      }
    }
    return false;
  }

  private ColoredFragment getSpacer(AtomicBoolean first) {
    if (first.get()) {
      return PresentationDataUtil.wideSpacer();
    } else {
      return PresentationDataUtil.spacer();
    }
  }
}
