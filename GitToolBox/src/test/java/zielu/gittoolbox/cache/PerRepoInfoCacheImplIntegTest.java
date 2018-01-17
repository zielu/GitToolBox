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
import org.eclipse.jgit.lib.StoredConfig;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import zielu.gittoolbox.status.Status;
import zielu.junit5.intellij.PlatformTestCaseExtension;

@Tag("integration")
@ExtendWith(PlatformTestCaseExtension.class)
class PerRepoInfoCacheImplIntegTest {
  private static Path myTestDataPath;

  @BeforeAll
  static void beforeAll() throws Exception {
    myTestDataPath = Paths.get(".", "testDataDynamic", "it", PerRepoInfoCacheImplIntegTest.class.getSimpleName());
    FileUtil.delete(myTestDataPath.toFile());
    initGit(myTestDataPath);
  }

  private static void initGit(Path testDataDir) throws Exception {
    Git git = Git.init().setDirectory(testDataDir.toFile()).setBare(false).call();
    StoredConfig config = git.getRepository().getConfig();
    config.load();
    config.setString("user", null, "name", "Jon Snow");
    config.setString("user", null, "email", "JonSnow@email.com");
    config.save();
    Files.write(testDataDir.resolve("file.txt"), Collections.singleton("abc"), Charsets.UTF_8);
    git.add().addFilepattern("file.txt").call();
    git.commit().setMessage("Initial commit").call();
    git.close();
  }

  private VirtualFile populateTestData(Project project, Module module) throws Exception {
    VirtualFile root = module.getModuleFile().getParent();
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
    return root;
  }

  @Test
  void repositoryMappingAdded(Project project, Module module) throws Exception {
    VirtualFile root = populateTestData(project, module);
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
    });
  }
}
