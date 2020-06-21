package zielu.gittoolbox.ui.projectview;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import git4idea.GitLocalBranch;
import git4idea.repo.GitRepository;
import java.util.List;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zielu.gittoolbox.cache.RepoInfo;
import zielu.gittoolbox.cache.RepoStatus;
import zielu.gittoolbox.config.DecorationPartConfig;
import zielu.gittoolbox.config.DecorationPartType;
import zielu.gittoolbox.config.GitToolBoxConfig2;
import zielu.gittoolbox.status.GitAheadBehindCount;
import zielu.gittoolbox.ui.StatusPresenters;

@ExtendWith(MockitoExtension.class)
abstract class NodeDecorationBaseTest {
  private static final String LOCATION_PATH = "/var/log/project";
  private static final String AHEAD_BEHIND = "1 // 1";
  private static final String BRANCH_NAME = "master";

  private static final String WS = "\\p{javaWhitespace}+";

  @Mock
  private GitRepository repository;
  @Mock
  private ProjectViewNode node;

  private GitToolBoxConfig2 config = new GitToolBoxConfig2();
  private GitAheadBehindCount count = GitAheadBehindCount.success(1, null, 1, null);
  private RepoInfo repoInfo = RepoInfo.create(RepoStatus.empty(), count, ImmutableList.of());

  @BeforeEach
  void before() {
    when(repository.getCurrentBranch()).thenReturn(new GitLocalBranch(BRANCH_NAME));
    config.setPresenter(StatusPresenters.text);
  }

  abstract NodeDecoration createDecoration(GitToolBoxConfig2 config, GitRepository repository, RepoInfo repoInfo);

  abstract DecorationData getDecorationData(PresentationData data);

  PresentationData presentationData(boolean location) {
    PresentationData data = presentationData();
    if (location) {
      data.setLocationString(LOCATION_PATH);
    }
    return data;
  }

  private PresentationData presentationData() {
    return new PresentationData();
  }

  PresentationData apply(PresentationData presentationData) {
    NodeDecoration decoration = createDecoration(config, repository, repoInfo);
    decoration.apply(node, presentationData);
    return presentationData;
  }

  private List<DecorationPartConfig> statusBeforeLocationParts() {
    return Lists.newArrayList(
        new DecorationPartConfig(DecorationPartType.BRANCH),
        new DecorationPartConfig(DecorationPartType.STATUS),
        new DecorationPartConfig(DecorationPartType.LOCATION)
    );
  }

  private String decorationPattern(String... parts) {
    return WS + String.join(WS, parts);
  }

  @Test
  @DisplayName("Location is shown before status")
  void locationBeforeStatus() {
    PresentationData presentationData = apply(presentationData(true));

    DecorationData decorationData = getDecorationData(presentationData);
    assertThat(decorationData.text).matches(decorationPattern("-", LOCATION_PATH, BRANCH_NAME, AHEAD_BEHIND));
  }

  @Test
  @DisplayName("Status is shown when location before status and location is null")
  void locationBeforeStatusNullLocation() {
    PresentationData presentationData = apply(presentationData(false));

    DecorationData decorationData = getDecorationData(presentationData);
    assertSoftly(softly -> {
      softly.assertThat(decorationData.text)
          .matches(decorationPattern(BRANCH_NAME, AHEAD_BEHIND));
      softly.assertThat(decorationData.locationString)
          .isNull();
    });
  }

  @Test
  @DisplayName("Status is shown before location")
  void statusBeforeLocation() {
    config.setDecorationParts(statusBeforeLocationParts());

    PresentationData presentationData = apply(presentationData(true));

    DecorationData decorationData = getDecorationData(presentationData);
    assertSoftly(softly -> {
      softly.assertThat(decorationData.text)
          .matches(decorationPattern(BRANCH_NAME, AHEAD_BEHIND));
      softly.assertThat(decorationData.locationString)
          .matches(decorationPattern(LOCATION_PATH));
    });
  }

  @Test
  @DisplayName("Status is shown when status before location and location is null")
  void statusBeforeLocationNullLocation() {
    config.setDecorationParts(statusBeforeLocationParts());

    PresentationData presentationData = apply(presentationData(false));

    DecorationData decorationData = getDecorationData(presentationData);
    assertSoftly(softly -> {
      softly.assertThat(decorationData.text)
          .matches(decorationPattern(BRANCH_NAME, AHEAD_BEHIND));
      softly.assertThat(decorationData.locationString)
          .isNull();
    });
  }

  @Test
  @DisplayName("Status is shown when location is hidden")
  void statusWhenLocationBeforeStatusAndLocationNotShown() {
    config.setDecorationParts(Lists.newArrayList(
        new DecorationPartConfig(DecorationPartType.BRANCH),
        new DecorationPartConfig(DecorationPartType.STATUS)
    ));

    PresentationData presentationData = apply(presentationData(true));

    DecorationData decorationData = getDecorationData(presentationData);
    assertThat(decorationData.text).matches(decorationPattern(BRANCH_NAME, AHEAD_BEHIND));
  }

  class DecorationData {
    final String text;
    final String locationString;

    DecorationData(String text, String locationString) {
      this.text = text;
      this.locationString = locationString;
    }
  }
}
