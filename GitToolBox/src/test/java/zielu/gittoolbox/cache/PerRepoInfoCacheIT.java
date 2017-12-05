package zielu.gittoolbox.cache;

import com.google.common.base.Charsets;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.PlatformTestCase;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.TestRunnerUtil;
import com.intellij.util.messages.MessageBusConnection;
import git4idea.GitUtil;
import git4idea.GitVcs;
import git4idea.repo.GitRepository;
import org.assertj.core.api.Assertions;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.StoredConfig;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import zielu.gittoolbox.GitToolBoxProject;
import zielu.gittoolbox.status.Status;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class PerRepoInfoCacheIT extends PlatformTestCase {
    private static Path myTestDataPath;

    @BeforeAll
    static void beforeAll() throws Exception{
        myTestDataPath = Paths.get(".", "testDataDynamic", "it", PerRepoInfoCacheIT.class.getSimpleName());
        FileUtil.delete(myTestDataPath.toFile());
        initGit(myTestDataPath);
    }

    @BeforeEach
    void before(TestInfo testInfo) throws Exception {
        setName(testInfo.getTestMethod().map(Method::getName).orElse("testNameNA"));
        runSetup();
    }

    private void runSetup() throws Exception {
        if (runInDispatchThread()) {
            TestRunnerUtil.replaceIdeEventQueueSafely();
            EdtTestUtil.runInEdtAndWait(this::setUp);
        } else {
            setUp();
        }
    }

    @AfterEach
    void after() throws Exception {
        runTearDown();
    }

    private void runTearDown() throws Exception {
        if (runInDispatchThread()) {
            EdtTestUtil.runInEdtAndWait(this::tearDown);
        } else {
            tearDown();
        }
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

    @Test
    void repositoryMappingAdded() throws Exception {
        VirtualFile root = myModule.getModuleFile().getParent();
        FileUtil.copyDir(myTestDataPath.toFile(), VfsUtil.virtualToIoFile(root));
        refreshRecursively(root);
        WriteCommandAction.runWriteCommandAction(myProject, () -> {
            ProjectLevelVcsManager vcsManager = ProjectLevelVcsManager.getInstance(myProject);
            vcsManager.setDirectoryMapping(root.getPath(), GitVcs.NAME);
            assertThat(LocalFileSystem.getInstance().findFileByPath(root.getPath())).isNotNull();
            GitRepository repository = GitUtil.getRepositoryManager(myProject).getRepositoryForRoot(root);
            assertThat(repository).isNotNull();
            PsiTestUtil.addContentRoot(myModule, root);
        });
        GitRepository repository = GitUtil.getRepositoryManager(myProject).getRepositoryForRoot(root);
        GitToolBoxProject.getInstance(myProject).perRepoStatusCache().getInfo(repository);
        MessageBusConnection connect = myProject.getMessageBus().connect();
        Exchanger<RepoInfo> exchange = new Exchanger<>();
        connect.subscribe(PerRepoInfoCache.CACHE_CHANGE, new PerRepoStatusCacheListener() {
            @Override
            public void stateChanged(@NotNull RepoInfo info, @NotNull GitRepository repository) {
                try {
                    exchange.exchange(info);
                } catch (InterruptedException e) {
                    Assertions.fail(e.getMessage(), e);
                }
            }
        });
        RepoInfo info = exchange.exchange(null, 10, TimeUnit.SECONDS);
        assertAll(
            () -> assertThat(info).isNotNull(),
            () -> assertThat(info.count()).isNotEmpty(),
            () -> assertThat(info.count().get().status()).isEqualTo(Status.NoRemote)
        );

    }
}
