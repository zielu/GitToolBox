package zielu.gittoolbox.ui.projectView;

import static org.assertj.core.api.Assertions.assertThat;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import git4idea.repo.GitRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import zielu.gittoolbox.GitToolBoxConfig;
import zielu.gittoolbox.status.GitAheadBehindCount;
import zielu.gittoolbox.ui.StatusPresenters;

public class LocationOnlyNodeDecorationTest {
    private static final String LOCATION = "/var/log/project";

    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();
    @Mock
    private GitRepository repository;
    @Mock
    private ProjectViewNode node;

    private GitToolBoxConfig config = new GitToolBoxConfig();
    private GitAheadBehindCount count = GitAheadBehindCount.success(1, null, 1, null);
    private NodeDecoration decoration;

    @Before
    public void before() {
        config.setPresenter(StatusPresenters.text);
        decoration = new LocationOnlyNodeDecoration(config, repository, count);
    }

    private PresentationData presentationData() {
        PresentationData data = new PresentationData();
        data.setLocationString(LOCATION);
        return data;
    }

    @Test
    public void apply() {
        PresentationData presentationData = presentationData();
        decoration.apply(node, presentationData);
        String location = presentationData.getLocationString();
        //TODO: why 2 spaces after dash ?
        assertThat(location).isEqualTo(LOCATION + " -  1 // 1");
    }
}
