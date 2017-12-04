package zielu.gittoolbox.ui.projectView;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import git4idea.GitLocalBranch;
import git4idea.repo.GitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import zielu.gittoolbox.cache.RepoInfo;
import zielu.gittoolbox.cache.RepoStatus;
import zielu.gittoolbox.config.GitToolBoxConfig;
import zielu.gittoolbox.status.GitAheadBehindCount;
import zielu.gittoolbox.ui.StatusPresenters;
import zielu.junit5.mockito.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LocationOnlyNodeDecorationTest {
    private static final String LOCATION = "/var/log/project";
    private static final String AHEAD_BEHIND = "1 // 1";
    private static final String BRANCH = "master";

    @Mock
    private GitRepository repository;
    @Mock
    private ProjectViewNode node;

    private GitToolBoxConfig config = new GitToolBoxConfig();
    private GitAheadBehindCount count = GitAheadBehindCount.success(1, null, 1, null);
    private RepoInfo repoInfo = RepoInfo.create(RepoStatus.empty(), count);
    private NodeDecoration decoration;

    @BeforeEach
    void before() {
        when(repository.getCurrentBranch()).thenReturn(new GitLocalBranch(BRANCH));
        config.setPresenter(StatusPresenters.text);
        decoration = new LocationOnlyNodeDecoration(config, repository, repoInfo);
    }

    private PresentationData presentationData(boolean location) {
        PresentationData data = presentationData();
        if (location) {
            data.setLocationString(LOCATION);
        }
        return data;
    }

    private PresentationData presentationData() {
        return new PresentationData();
    }

    private PresentationData apply(PresentationData presentationData) {
        decoration.apply(node, presentationData);
        return presentationData;
    }

    @Test
    @DisplayName("Location is shown before status")
    void locationBeforeStatus() {
        config.showProjectViewStatusBeforeLocation = true;
        PresentationData presentationData = apply(presentationData(true));
        String location = presentationData.getLocationString();
        assertEquals(BRANCH + " " + AHEAD_BEHIND + " - " + LOCATION, location);
    }

    @Test
    @DisplayName("Status is shown when location before status and location is null")
    void locationBeforeStatusNullLocation() {
        config.showProjectViewStatusBeforeLocation = true;
        PresentationData presentationData = apply(presentationData(false));
        String location = presentationData.getLocationString();
        assertEquals(BRANCH + " " + AHEAD_BEHIND, location);
    }

    @Test
    @DisplayName("Status is shown before location")
    void statusBeforeLocation() {
        PresentationData presentationData = apply(presentationData(true));
        String location = presentationData.getLocationString();
        assertEquals(LOCATION + " - " + BRANCH + " " + AHEAD_BEHIND, location);
    }

    @Test
    @DisplayName("Status is shown when status before location and location is null")
    void statusBeforeLocationNullLocation() {
        PresentationData presentationData = apply(presentationData(false));
        String location = presentationData.getLocationString();
        assertEquals(BRANCH + " " + AHEAD_BEHIND, location);
    }

    @Test
    @DisplayName("Status is shown when location is not shown and location before status")
    void statusWhenLocationBeforeStatusAndLocationNotShown() {
        config.showProjectViewLocationPath = false;
        PresentationData presentationData = apply(presentationData(true));
        String location = presentationData.getLocationString();
        assertEquals(BRANCH + " " + AHEAD_BEHIND, location);
    }

    @Test
    @DisplayName("Status is shown when location is not shown and status before location")
    void statusWhenStatusBeforeLocationAndLocationNotShown() {
        config.showProjectViewLocationPath = false;
        config.showProjectViewStatusBeforeLocation = true;
        PresentationData presentationData = apply(presentationData(true));
        String location = presentationData.getLocationString();
        assertEquals(BRANCH + " " + AHEAD_BEHIND, location);
    }
}
