package zielu.gittoolbox.ui.projectview;

import static zielu.gittoolbox.config.DecorationPartType.BRANCH;
import static zielu.gittoolbox.config.DecorationPartType.CHANGED_COUNT;
import static zielu.gittoolbox.config.DecorationPartType.STATUS;
import static zielu.gittoolbox.config.DecorationPartType.TAGS_ON_HEAD;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.util.treeView.PresentableNodeDescriptor.ColoredFragment;
import com.intellij.ui.SimpleTextAttributes;
import git4idea.repo.GitRepository;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.cache.RepoInfo;
import zielu.gittoolbox.config.DecorationPartType;
import zielu.gittoolbox.ui.ExtendedRepoInfo;
import zielu.gittoolbox.ui.util.PresentationDataUtil;

public class ColoredNodeDecoration extends NodeDecorationBase {
  private static final Map<DecorationPartType, Function<ColoredNodeDecoration, ColoredFragment>> DECORATORS =
      new EnumMap<>(DecorationPartType.class);

  static {
    DECORATORS.put(BRANCH, decoration -> {
      String value = decoration.coloredUi.getDecorationPartText(decoration.getBranchText(), BRANCH);
      if (StringUtils.isNotBlank(value)) {
        return new ColoredFragment(value, decoration.getBranchAttributes());
      }
      return null;
    });
    DECORATORS.put(STATUS, decoration -> {
      String value = decoration.coloredUi.getDecorationPartText(decoration.getCountText(), STATUS);
      if (StringUtils.isNotBlank(value)) {
        return new ColoredFragment(value, decoration.coloredUi.getStatusAttributes());
      }
      return null;
    });
    DECORATORS.put(TAGS_ON_HEAD, decoration -> {
      String value = decoration.coloredUi.getDecorationPartText(decoration.getTagsText(), TAGS_ON_HEAD);
      if (StringUtils.isNotBlank(value)) {
        return new ColoredFragment(value, decoration.coloredUi.getHeadTagsAttributes());
      }
      return null;
    });
    DECORATORS.put(CHANGED_COUNT, decoration -> {
      String value = decoration.coloredUi.getDecorationPartText(decoration.getChangedCountText(), CHANGED_COUNT);
      if (StringUtils.isNotBlank(value)) {
        return new ColoredFragment(value, decoration.coloredUi.getChangedCountAttributes());
      }
      return null;
    });
  }

  private static final Function<ColoredNodeDecoration, ColoredFragment> EMPTY_DECORATOR = decoration -> null;

  private ColoredNodeDecorationUi coloredUi;

  public ColoredNodeDecoration(@NotNull ColoredNodeDecorationUi ui,
                               @NotNull GitRepository repo,
                               @NotNull RepoInfo repoInfo,
                               @NotNull ExtendedRepoInfo extendedRepoInfo) {
    super(ui, repo, repoInfo, extendedRepoInfo);
    coloredUi = ui;
  }

  @Nullable
  private ColoredFragment getFragmentFor(DecorationPartType type) {
    return DECORATORS.getOrDefault(type, EMPTY_DECORATOR).apply(this);
  }

  private SimpleTextAttributes getBranchAttributes() {
    if (isTrackingBranch()) {
      if (isMaster()) {
        return coloredUi.getMasterWithRemoteAttributes();
      } else {
        return coloredUi.getRemoteBranchAttributes();
      }
    } else {
      if (isMaster()) {
        return coloredUi.getMasterLocalAttributes();
      } else {
        return coloredUi.getLocalBranchAttributes();
      }
    }
  }

  private void setName(PresentationData data) {
    if (PresentationDataUtil.hasEmptyColoredTextValue(data)) {
      String presentableText = data.getPresentableText();
      if (presentableText != null) {
        data.addText(presentableText, coloredUi.getNameAttributes());
      }
    }
  }

  @Override
  public boolean apply(ProjectViewNode node, PresentationData data) {
    setName(data);
    String locationString = data.getLocationString();
    if (ui.hasLocationPart()) {
      if (ui.isLocationPartLast() && locationString != null) {
        String location = ui.getDecorationPartText(locationString, DecorationPartType.LOCATION);
        if (location != null) {
          data.setLocationString(" " + location);
        }
      }
    } else {
      if (locationString != null) {
        data.setTooltip(locationString);
      }
      data.setLocationString("");
    }

    boolean first = true;
    Collection<DecorationPartType> types = ui.getDecorationTypes();
    for (DecorationPartType type : types) {
      if (type == DecorationPartType.LOCATION && !ui.isLocationPartLast()) {
        if (locationString != null) {
          String location = " " + coloredUi.getDecorationPartText(locationString, type);
          data.addText(getSpacer(first));
          data.addText(new ColoredFragment(location, coloredUi.getLocationAttributes()));
          data.setLocationString("");
          first = false;
        }
      } else {
        ColoredFragment fragment = getFragmentFor(type);
        if (fragment != null) {
          data.addText(getSpacer(first));
          data.addText(fragment);
          first = false;
        }
      }
    }
    return false;
  }

  private ColoredFragment getSpacer(boolean first) {
    if (first) {
      return PresentationDataUtil.wideSpacer;
    } else {
      return PresentationDataUtil.spacer;
    }
  }
}
