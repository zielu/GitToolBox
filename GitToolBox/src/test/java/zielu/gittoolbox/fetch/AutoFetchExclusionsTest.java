package zielu.gittoolbox.fetch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.intellij.mock.MockVirtualFile;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.repo.GitRemote;
import git4idea.repo.GitRepository;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import zielu.TestType;
import zielu.gittoolbox.config.AutoFetchExclusionConfig;
import zielu.gittoolbox.config.RemoteConfig;

@Tag(TestType.FAST)
@ExtendWith(MockitoExtension.class)
class AutoFetchExclusionsTest {
  private final VirtualFile repoRoot = new MockVirtualFile("repo-root");
  private GitRepository repository;

  @BeforeEach
  void beforeEach() {
    repository = mock(GitRepository.class);
    when(repository.getRoot()).thenReturn(repoRoot);
  }

  @Test
  void repoWithoutSpecifiedRemotesIsExcluded() {
    //given
    Map<String, AutoFetchExclusionConfig> exclusionsMap = ImmutableMap.of(repoRoot.getUrl(),
        new AutoFetchExclusionConfig(repoRoot.getUrl()));
    AutoFetchExclusions exclusions = new AutoFetchExclusions(() -> exclusionsMap);

    //when
    List<GitRepository> result = exclusions.apply(Collections.singletonList(repository));

    //then
    assertThat(result).isEmpty();
  }

  @Test
  void notConfiguredRepoIsNotExcluded() {
    //given
    AutoFetchExclusions exclusions = new AutoFetchExclusions(ImmutableMap::of);

    //when
    List<GitRepository> result = exclusions.apply(Collections.singletonList(repository));

    //then
    assertThat(result).containsOnly(repository);
  }

  @Test
  void repoWithSpecifiedRemoteExcludesItFromRemotes() {
    //given
    GitRemote upstream = new GitRemote("upstream", Collections.emptyList(), Collections.emptyList(),
        Collections.emptyList(), Collections.emptyList());
    GitRemote origin = new GitRemote("origin", Collections.emptyList(), Collections.emptyList(),
        Collections.emptyList(), Collections.emptyList());
    when(repository.getRemotes()).thenReturn(Lists.newArrayList(upstream, origin));

    Map<String, AutoFetchExclusionConfig> exclusionsMap = ImmutableMap.of(repoRoot.getUrl(),
        new AutoFetchExclusionConfig(repoRoot.getUrl(),
            Collections.singletonList(new RemoteConfig(upstream.getName()))));
    AutoFetchExclusions exclusions = new AutoFetchExclusions(() -> exclusionsMap);

    //when
    List<GitRepository> result = exclusions.apply(Collections.singletonList(repository));
    assertSoftly(softly -> {
      softly.assertThat(result).hasSize(1);
      softly.assertThat(result.get(0).getRemotes()).containsOnly(origin);
    });
  }
}
