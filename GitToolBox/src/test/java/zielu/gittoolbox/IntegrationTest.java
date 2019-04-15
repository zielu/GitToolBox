package zielu.gittoolbox;

import static com.intellij.testFramework.UsefulTestCase.refreshRecursively;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.google.common.base.Charsets;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsDirectoryMapping;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.PsiTestUtil;
import git4idea.GitUtil;
import git4idea.GitVcs;
import git4idea.repo.GitRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import org.eclipse.jgit.api.Git;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import zielu.gittoolbox.blame.BlameListener;
import zielu.gittoolbox.blame.BlameService;
import zielu.gittoolbox.cache.PerRepoInfoCache;
import zielu.gittoolbox.cache.PerRepoStatusCacheListener;
import zielu.gittoolbox.cache.RepoInfo;
import zielu.gittoolbox.revision.RevisionInfo;
import zielu.gittoolbox.status.Status;
import zielu.intellij.test.Awaiter;
import zielu.junit5.intellij.extension.git.GitTest;
import zielu.junit5.intellij.extension.git.GitTestExtension;
import zielu.junit5.intellij.extension.git.GitTestSetup;
import zielu.junit5.intellij.extension.platform.PlatformTest;
import zielu.junit5.intellij.extension.platform.PlatformTestCaseExtension;
import zielu.junit5.intellij.extension.resources.ExternalPath;
import zielu.junit5.intellij.extension.resources.ResourcesExtension;

@Tag(TestType.INTEGRATION)
@ExtendWith(PlatformTestCaseExtension.class)
@ExtendWith(GitTestExtension.class)
@ExtendWith(ResourcesExtension.class)
class IntegrationTest {
  private static final String FILE_NAME = "file.txt";
  private static final String TAG = "1.0.0";
  private static Path myTestDataPath;

  @BeforeAll
  static void beforeAll(GitTest gitTest,
                        @ExternalPath({".", "testDataDynamic", "it", "IntegrationTest"}) Path testDataPath)
      throws Exception {
    myTestDataPath = testDataPath;
    initGit(gitTest, myTestDataPath);
  }

  private static void initGit(GitTest gitTest, Path rootPath) throws Exception {
    gitTest.prepare(new GitTestSetup() {
      @Override
      public Path getRootPath() {
        return rootPath;
      }

      @Override
      public void setup(Git git) throws Exception {
        Files.write(rootPath.resolve(FILE_NAME), Collections.singleton("abc"), Charsets.UTF_8);
        git.add().addFilepattern(FILE_NAME).call();
        git.commit().setMessage("Initial commit").call();
        git.tag().setName(TAG).call();
      }
    });
  }

  @BeforeEach
  void populateTestData(Project project, Module module) throws Exception {
    VirtualFile root = getRoot(module);
    FileUtil.copyDir(myTestDataPath.toFile(), VfsUtil.virtualToIoFile(root));
    WriteCommandAction.runWriteCommandAction(project, () -> {
      refreshRecursively(root);
      ProjectLevelVcsManager vcsManager = ProjectLevelVcsManager.getInstance(project);
      String rootPath = root.getPath();
      vcsManager.setDirectoryMappings(Collections.singletonList(new VcsDirectoryMapping(rootPath, GitVcs.NAME)));
      assertThat(LocalFileSystem.getInstance().findFileByPath(rootPath)).isNotNull();
      GitRepository repository = GitUtil.getRepositoryManager(project).getRepositoryForRoot(root);
      assertThat(repository).isNotNull();
      PsiTestUtil.addContentRoot(module, root);
    });
  }

  private VirtualFile getRoot(Module module) {
    return module.getModuleFile().getParent();
  }

  @Test
  void perRepoInfoCacheLoadsDataIfCalled(Project project, Module module, PlatformTest test) throws Exception {
    VirtualFile root = getRoot(module);
    GitRepository repository = GitUtil.getRepositoryManager(project).getRepositoryForRoot(root);
    Awaiter awaitInfoUpdate = new Awaiter();
    test.subscribe(PerRepoInfoCache.CACHE_CHANGE, new PerRepoStatusCacheListener() {
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
      softly.assertThat(info.count()).isNotEmpty();
      softly.assertThat(info.count().get().status()).isEqualTo(Status.NO_REMOTE);
      softly.assertThat(info.tags()).containsOnly(TAG);
    });
  }

  @Test
  void fileBlameReturnsDataIfCalled(Project project, Module module) {
    VirtualFile file = getRoot(module).findChild(FILE_NAME);
    RevisionInfo fileBlame = BlameService.getInstance(project).getFileBlame(file);

    assertSoftly(softly -> {
      softly.assertThat(fileBlame.isNotEmpty()).isTrue();
    });
  }

  @Test
  void lineBlameReturnsDataIfCalled(Project project, Module module, PlatformTest test) throws InterruptedException {
    VirtualFile file = getRoot(module).findChild(FILE_NAME);
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
}
