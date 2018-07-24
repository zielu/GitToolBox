package zielu.gittoolbox.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static zielu.intellij.test.MockVfsUtil.createDir;

import com.google.common.collect.ImmutableList;
import com.intellij.mock.MockVirtualFile;
import git4idea.repo.GitRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zielu.gittoolbox.metrics.Metrics;
import zielu.gittoolbox.metrics.MockMetrics;

@Tag("fast")
@ExtendWith({MockitoExtension.class})
class VirtualFileRepoCacheImplTest {
  @Mock(stubOnly = true)
  private GitRepository repository;
  @Mock
  private VirtualFileRepoCacheController controller;

  private Metrics mockMetrics = new MockMetrics();

  private MockVirtualFile repositoryRoot = createDir("repoRoot");

  private VirtualFileRepoCacheImpl cache;

  @BeforeEach
  void beforeEach() {
    when(controller.getMetrics()).thenReturn(mockMetrics);
    cache = new VirtualFileRepoCacheImpl(controller);
    cache.initComponent();
  }

  private void mockRepository() {
    when(repository.getRoot()).thenReturn(repositoryRoot);
  }

  @AfterEach
  void afterEach() {
    cache.disposeComponent();
  }

  @Test
  void getRepoForRootShouldReturnNullIfEmpty() {
    assertThat(cache.getRepoForRoot(createDir("root"))).isNull();
  }

  @Test
  void getRepoForRootShouldReturnRepositoryForRoot() {
    mockRepository();

    cache.updatedRepoList(ImmutableList.of(repository));
    assertThat(cache.getRepoForRoot(repositoryRoot)).isEqualTo(repository);
  }

  @Test
  void getRepoForDirShouldReturnNullIfEmpty() {
    assertThat(cache.getRepoForDir(createDir("dir"))).isNull();
  }

  @Test
  void getRepoForDirShouldReturnRepositoryForDirInRoot() {
    mockRepository();

    cache.updatedRepoList(ImmutableList.of(repository));
    MockVirtualFile dirInRoot = createDir(repositoryRoot,"dirInRoot");
    assertThat(cache.getRepoForDir(dirInRoot)).isEqualTo(repository);
  }

  @Test
  void getRepoForDirShouldReturnSameRepositoryForDirInRootIfCalledMoreThanOnce() {
    mockRepository();

    cache.updatedRepoList(ImmutableList.of(repository));
    MockVirtualFile dirInRoot = createDir(repositoryRoot,"dirInRoot");
    GitRepository repo1 = cache.getRepoForDir(dirInRoot);
    GitRepository repo2 = cache.getRepoForDir(dirInRoot);
    assertThat(repo1).isEqualTo(repo2);
  }

  @Test
  void getRepoForDirShouldReturnRepositoryForDirInDirInRootBottomUp() {
    mockRepository();

    cache.updatedRepoList(ImmutableList.of(repository));
    MockVirtualFile dirInRoot = createDir(repositoryRoot, "dirInRoot");
    MockVirtualFile dirInDirInRoot = createDir(dirInRoot, "dirInDirInRoot");
    assertThat(cache.getRepoForDir(dirInDirInRoot)).isEqualTo(repository);
  }

  @Test
  void getRepoForDirShouldReturnRepositoryForDirInDirInRootTopDown() {
    mockRepository();

    cache.updatedRepoList(ImmutableList.of(repository));
    MockVirtualFile dirInRoot = createDir(repositoryRoot, "dirInRoot");
    MockVirtualFile dirInDirInRoot = createDir(dirInRoot, "dirInDirInRoot");
    assertSoftly(softly -> {
      softly.assertThat(cache.getRepoForDir(dirInRoot)).isEqualTo(repository);
      softly.assertThat(cache.getRepoForDir(dirInDirInRoot)).isEqualTo(repository);
    });

  }

  @Test
  void updatedRepoListPublishesToMessageBus() {
    mockRepository();

    cache.updatedRepoList(ImmutableList.of(repository));
    verify(controller).fireCacheChanged();
  }
}