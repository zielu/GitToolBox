package zielu.gittoolbox.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static zielu.intellij.test.MockVfsUtil.createDir;

import com.google.common.collect.ImmutableList;
import com.intellij.mock.MockVirtualFile;
import com.intellij.openapi.project.Project;
import git4idea.repo.GitRepository;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import zielu.junit5.intellij.IdeaMocks;
import zielu.junit5.intellij.IdeaMocksExtension;
import zielu.junit5.mockito.MockitoExtension;

@ExtendWith({MockitoExtension.class, IdeaMocksExtension.class})
class VirtualFileRepoCacheImplTest {
  @Mock
  private GitRepository repository;
  private MockVirtualFile repositoryRoot = createDir("repoRoot");

  private VirtualFileRepoCacheImpl cache;

  @BeforeEach
  void beforeEach(Project project) {
    cache = new VirtualFileRepoCacheImpl(project);
    cache.initComponent();
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
    cache.updatedRepoList(ImmutableList.of(repository));
    assertThat(cache.getRepoForRoot(repositoryRoot)).isSameAs(repository);
  }

  @Test
  void getRepoForDirShouldReturnNullIfEmpty() {
    assertThat(cache.getRepoForDir(createDir("dir"))).isNull();
  }

  @Test
  void getRepoForDirShouldReturnRepositoryForDirInRoot() {
    cache.updatedRepoList(ImmutableList.of(repository));
    MockVirtualFile dirInRoot = createDir(repositoryRoot,"dirInRoot");
    assertThat(cache.getRepoForDir(dirInRoot)).isSameAs(repository);
  }

  @Test
  void getRepoForDirShouldReturnRepositoryForDirInDirInRootBottomUp() {
    cache.updatedRepoList(ImmutableList.of(repository));
    MockVirtualFile dirInRoot = createDir(repositoryRoot, "dirInRoot");
    MockVirtualFile dirInDirInRoot = createDir(dirInRoot, "dirInDirInRoot");
    assertThat(cache.getRepoForDir(dirInDirInRoot)).isSameAs(repository);
  }

  @Test
  void getRepoForDirShouldReturnRepositoryForDirInDirInRootTopDown() {
    cache.updatedRepoList(ImmutableList.of(repository));
    MockVirtualFile dirInRoot = createDir(repositoryRoot, "dirInRoot");
    MockVirtualFile dirInDirInRoot = createDir(dirInRoot, "dirInDirInRoot");
    assertSoftly(softly -> {
      softly.assertThat(cache.getRepoForDir(dirInRoot)).isSameAs(repository);
      softly.assertThat(cache.getRepoForDir(dirInDirInRoot)).isSameAs(repository);
    });

  }

  @Test
  void updatedRepoListPublishesToMessageBus(IdeaMocks mocks) {
    VirtualFileCacheListener listener = mocks.mockListener(VirtualFileCacheListener.class);
    cache.updatedRepoList(ImmutableList.of(repository));
    verify(listener).updated();
  }
}