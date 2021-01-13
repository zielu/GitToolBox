package zielu.gittoolbox;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.google.common.base.Charsets;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.GitUtil;
import git4idea.repo.GitRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import org.eclipse.jgit.api.Git;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import zielu.gittoolbox.blame.BlameCache;
import zielu.gittoolbox.blame.BlameListener;
import zielu.gittoolbox.blame.BlameService;
import zielu.gittoolbox.cache.PerRepoInfoCache;
import zielu.gittoolbox.cache.PerRepoStatusCacheListener;
import zielu.gittoolbox.cache.RepoInfo;
import zielu.gittoolbox.revision.RevisionInfo;
import zielu.gittoolbox.status.Status;
import zielu.intellij.test.Awaiter;
import zielu.junit5.intellij.extension.git.GitProject;
import zielu.junit5.intellij.extension.git.GitTest;
import zielu.junit5.intellij.extension.git.GitTestExtension;
import zielu.junit5.intellij.extension.git.GitTestSetup;
import zielu.junit5.intellij.extension.platform.HeavyPlatformTest;
import zielu.junit5.intellij.extension.platform.HeavyPlatformTestCaseExtension;
import zielu.junit5.intellij.extension.resources.ExternalPath;
import zielu.junit5.intellij.extension.resources.ResourcesExtension;
import zielu.junit5.intellij.util.TestUtil;

@zielu.IntegrationTest
@ExtendWith({
    HeavyPlatformTestCaseExtension.class,
    GitTestExtension.class,
    ResourcesExtension.class
})
class IntegrationTest {
  private static final String FILE_NAME = "file.txt";
  private static final String TAG = "1.0.0";
  private static Path myTestDataPath;

  @BeforeAll
  static void beforeAll(GitTest gitTest,
                        @ExternalPath({".", "testDataDynamic", "it", "IntegrationTest"}) Path testDataPath) {
    myTestDataPath = testDataPath;
    initGit(gitTest, myTestDataPath);
  }

  private static void initGit(GitTest gitTest, Path rootPath) {
    gitTest.prepare(new GitTestSetup() {
      @NotNull
      @Override
      public Path getRootPath() {
        return rootPath;
      }

      @Override
      public void setup(@NotNull Git git) throws Exception {
        Files.write(rootPath.resolve(FILE_NAME), Collections.singleton("abc"), Charsets.UTF_8);
        git.add().addFilepattern(FILE_NAME).call();
        git.commit().setMessage("Initial commit").call();
        git.tag().setName(TAG).call();
      }
    });
  }

  @BeforeEach
  void populateTestData(Project project, Module module) {
    GitProject.setup(project, module, myTestDataPath);
  }

  @Test
  void perRepoInfoCacheLoadsDataIfCalled(Project project, Module module, HeavyPlatformTest test) {
    VirtualFile root = TestUtil.findVfForPath(GitProject.rootFor(module));
    GitRepository repository = GitUtil.getRepositoryManager(project).getRepositoryForRoot(root);
    Awaiter awaitInfoUpdate = new Awaiter();
    test.subscribe(PerRepoInfoCache.CACHE_CHANGE_TOPIC, new PerRepoStatusCacheListener() {
      @Override
      public void stateChanged(@NotNull RepoInfo info, @NotNull GitRepository repository) {
        awaitInfoUpdate.satisfied();
      }
    });
    PerRepoInfoCache.getInstance(project).getInfo(repository);
    awaitInfoUpdate.await();
    RepoInfo info = PerRepoInfoCache.getInstance(project).getInfo(repository);

    assertSoftly(softly -> {
      softly.assertThat(info).isNotNull();
      softly.assertThat(info.maybeCount()).isNotEmpty();
      softly.assertThat(info.maybeCount().get().status()).isEqualTo(Status.NO_REMOTE);
      softly.assertThat(info.tags()).containsOnly(TAG);
    });
  }

  @Test
  void lineBlameReturnsDataIfCalled(Project project, Module module, HeavyPlatformTest test) {
    VirtualFile file = TestUtil.findVfForPath(GitProject.rootFor(module)).findChild(FILE_NAME);
    Document document = test.getDocument(file);

    Awaiter awaitBlameUpdate = new Awaiter();
    test.subscribe(BlameService.BLAME_UPDATE, new BlameListener() {
      @Override
      public void blameUpdated(@NotNull VirtualFile file) {
        awaitBlameUpdate.satisfied();
      }
    });
    BlameService.getInstance(project).getDocumentLineIndexBlame(document, file, 0);
    awaitBlameUpdate.await();

    RevisionInfo lineInfo = BlameService.getInstance(project).getDocumentLineIndexBlame(document, file, 0);
    assertSoftly(softly -> {
      softly.assertThat(lineInfo.isNotEmpty()).isTrue();
    });
  }

  @Test
  void lineBlameReturnsSameRevisionIfRepoRefreshedButNotChanged(Project project,
                                                                Module module,
                                                                HeavyPlatformTest test) {
    VirtualFile root = TestUtil.findVfForPath(GitProject.rootFor(module));
    VirtualFile file = root.findChild(FILE_NAME);
    Document document = test.getDocument(file);

    Awaiter awaitBlameUpdate = new Awaiter();
    test.subscribe(BlameService.BLAME_UPDATE, new BlameListener() {
      @Override
      public void blameUpdated(@NotNull VirtualFile file) {
        awaitBlameUpdate.satisfied();
      }
    });
    BlameService blameService = BlameService.getInstance(project);
    blameService.getDocumentLineIndexBlame(document, file, 0);
    awaitBlameUpdate.await();

    RevisionInfo firstLineInfo = blameService.getDocumentLineIndexBlame(document, file, 0);

    BlameCache.getExistingInstance(project).orElseThrow(IllegalStateException::new).refreshForRoot(root);
    RevisionInfo secondLineInfo = blameService.getDocumentLineIndexBlame(document, file, 0);

    assertSoftly(softly -> {
      softly.assertThat(firstLineInfo.getRevisionNumber()).isEqualTo(secondLineInfo.getRevisionNumber());
    });
  }
}
