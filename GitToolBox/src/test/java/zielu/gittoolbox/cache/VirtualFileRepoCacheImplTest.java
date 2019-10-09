package zielu.gittoolbox.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static zielu.intellij.test.MockVfsUtil.createDir;
import static zielu.intellij.test.MockVfsUtil.createFile;

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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import zielu.TestType;
import zielu.intellij.test.MockUtil;

@Tag(TestType.FAST)
@ExtendWith({MockitoExtension.class})
@MockitoSettings(strictness = Strictness.WARN)
class VirtualFileRepoCacheImplTest {
  @Mock(stubOnly = true)
  private GitRepository repositoryMock;
  @Mock
  private VirtualFileRepoCacheLocalGateway gatewayMock;

  private MockVirtualFile repositoryRoot = createDir("repoRoot");

  private VirtualFileRepoCacheImpl cache;

  @BeforeEach
  void beforeEach() {
    when(gatewayMock.repoForDirCacheTimer(any())).then(MockUtil.callSupplier());
    cache = new VirtualFileRepoCacheImpl(gatewayMock);
  }

  private void mockRepository() {
    when(repositoryMock.getRoot()).thenReturn(repositoryRoot);
  }

  @AfterEach
  void afterEach() {
    cache.dispose();
  }

  @Test
  void getRepoForRootShouldReturnNullIfEmpty() {
    assertThat(cache.getRepoForRoot(createDir("root"))).isNull();
  }

  @Test
  void getRepoForRootShouldReturnRepositoryForRoot() {
    setupRepo();

    assertThat(cache.getRepoForRoot(repositoryRoot)).isEqualTo(repositoryMock);
  }

  void setupRepo() {
    mockRepository();
    cache.updatedRepoList(ImmutableList.of(repositoryMock));
  }

  @Test
  void getRepoForDirShouldReturnNullIfEmpty() {
    assertThat(cache.getRepoForDir(createDir("dir"))).isNull();
  }

  @Test
  void getRepoForDirShouldReturnRepositoryForDirInRoot() {
    setupRepo();

    MockVirtualFile dirInRoot = createDir(repositoryRoot, "dirInRoot");
    assertThat(cache.getRepoForDir(dirInRoot)).isEqualTo(repositoryMock);
  }

  @Test
  void getRepoForDirShouldReturnSameRepositoryForDirInRootIfCalledMoreThanOnce() {
    setupRepo();

    MockVirtualFile dirInRoot = createDir(repositoryRoot, "dirInRoot");
    GitRepository repo1 = cache.getRepoForDir(dirInRoot);
    GitRepository repo2 = cache.getRepoForDir(dirInRoot);
    assertThat(repo1).isEqualTo(repo2);
  }

  @Test
  void getRepoForDirShouldReturnRepositoryForDirInDirInRootBottomUp() {
    setupRepo();

    MockVirtualFile dirInRoot = createDir(repositoryRoot, "dirInRoot");
    MockVirtualFile dirInDirInRoot = createDir(dirInRoot, "dirInDirInRoot");
    assertThat(cache.getRepoForDir(dirInDirInRoot)).isEqualTo(repositoryMock);
  }

  @Test
  void getRepoForDirShouldReturnRepositoryForDirInDirInRootTopDown() {
    setupRepo();

    MockVirtualFile dirInRoot = createDir(repositoryRoot, "dirInRoot");
    MockVirtualFile dirInDirInRoot = createDir(dirInRoot, "dirInDirInRoot");
    assertSoftly(softly -> {
      softly.assertThat(cache.getRepoForDir(dirInRoot))
          .isEqualTo(repositoryMock);
      softly.assertThat(cache.getRepoForDir(dirInDirInRoot))
          .isEqualTo(repositoryMock);
    });

  }

  @Test
  void updatedRepoListPublishesToMessageBus() {
    setupRepo();

    verify(gatewayMock).fireCacheChanged();
  }

  @Test
  void isUnderGitRootShouldReturnTrueForDirUnderRoot() {
    setupRepo();

    MockVirtualFile dirInRoot = createDir(repositoryRoot, "dirInRoot");
    assertSoftly(softly -> {
      softly.assertThat(cache.isUnderGitRoot(dirInRoot))
          .isTrue();
    });
  }

  @Test
  void isUnderGitRootShouldReturnFalseForDirNotUnderRoot() {
    setupRepo();

    MockVirtualFile dirInRoot = createDir("dirInRoot");
    assertSoftly(softly -> {
      softly.assertThat(cache.isUnderGitRoot(dirInRoot))
          .isFalse();
    });
  }

  @Test
  void isUnderGitRootShouldReturnTrueForFileUnderRoot() {
    setupRepo();

    MockVirtualFile fileInRoot = createFile(repositoryRoot, "fileInRoot.txt");
    assertSoftly(softly -> {
      softly.assertThat(cache.isUnderGitRoot(fileInRoot))
          .isTrue();
    });
  }

  @Test
  void isUnderGitRootShouldReturnFalseForFileNotUnderRoot() {
    setupRepo();

    MockVirtualFile fileInRoot = createFile("fileInRoot.txt");
    assertSoftly(softly -> {
      softly.assertThat(cache.isUnderGitRoot(fileInRoot))
          .isFalse();
    });
  }
}
