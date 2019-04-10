package zielu.gittoolbox.cache;

import static com.intellij.testFramework.UsefulTestCase.refreshRecursively;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.google.common.base.Charsets;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.util.messages.MessageBusConnection;
import git4idea.GitUtil;
import git4idea.GitVcs;
import git4idea.repo.GitRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import org.eclipse.jgit.api.Git;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import zielu.gittoolbox.TestType;
import zielu.gittoolbox.blame.BlameService;
import zielu.gittoolbox.revision.RevisionInfo;
import zielu.gittoolbox.status.Status;
import zielu.junit5.intellij.GitTestExtension;
import zielu.junit5.intellij.GitTestExtension.GitTest;
import zielu.junit5.intellij.GitTestExtension.GitTestSetup;
import zielu.junit5.intellij.PlatformTestCaseExtension;

@Tag(TestType.INTEGRATION)
@ExtendWith(PlatformTestCaseExtension.class)
@ExtendWith(GitTestExtension.class)
class PerRepoInfoCacheImplITest {
  private static final String FILE_NAME = "file.txt";
  private static final String TAG = "1.0.0";
  private static Path myTestDataPath;

  @BeforeAll
  static void beforeAll(GitTest gitTest) throws Exception {
    myTestDataPath = Paths.get(".", "testDataDynamic", "it", PerRepoInfoCacheImplITest.class.getSimpleName())
        .normalize()
        .toAbsolutePath();
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
    refreshRecursively(root);
    WriteCommandAction.runWriteCommandAction(project, () -> {
      ProjectLevelVcsManager vcsManager = ProjectLevelVcsManager.getInstance(project);
      vcsManager.setDirectoryMapping(root.getPath(), GitVcs.NAME);
      assertThat(LocalFileSystem.getInstance().findFileByPath(root.getPath())).isNotNull();
      GitRepository repository = GitUtil.getRepositoryManager(project).getRepositoryForRoot(root);
      assertThat(repository).isNotNull();
      PsiTestUtil.addContentRoot(module, root);
    });
  }

  private VirtualFile getRoot(Module module) {
    return module.getModuleFile().getParent();
  }

  @Test
  void perRepoInfoCacheLoadsDataIfCalled(Project project, Module module) throws Exception {
    VirtualFile root = getRoot(module);
    GitRepository repository = GitUtil.getRepositoryManager(project).getRepositoryForRoot(root);
    MessageBusConnection connect = project.getMessageBus().connect();
    Exchanger<RepoInfo> exchange = new Exchanger<>();
    connect.subscribe(PerRepoInfoCache.CACHE_CHANGE, new PerRepoStatusCacheListener() {
      @Override
      public void stateChanged(@NotNull RepoInfo info, @NotNull GitRepository repository) {
        try {
          exchange.exchange(info);
        } catch (InterruptedException e) {
          fail(e.getMessage(), e);
        }
      }
    });
    PerRepoInfoCache.getInstance(project).getInfo(repository);
    RepoInfo info = exchange.exchange(null, 30, TimeUnit.SECONDS);
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
}
