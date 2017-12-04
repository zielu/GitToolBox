package zielu.gittoolbox.ui.projectView;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.intellij.dvcs.repo.Repository;
import com.intellij.dvcs.repo.VcsRepositoryManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsDirectoryMapping;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.impl.LightTempDirTestFixtureImpl;
import git4idea.GitUtil;
import git4idea.GitVcs;
import git4idea.repo.GitRepository;
import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import zielu.gittoolbox.GitToolBoxProject;
import zielu.gittoolbox.cache.PerRepoInfoCache;
import zielu.junit5.intellij.ContainsTempFiles;
import zielu.junit5.intellij.IdeaLightExtension;
import zielu.junit5.mockito.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(IdeaLightExtension.class)
@ExtendWith(MockitoExtension.class)
@ContainsTempFiles
class NodeDecorationIT {
    private static Path myTestDataPath;
    private static CodeInsightTestFixture myFixture;
    private static Module myModule;
    private static Project myProject;
    private static VirtualFile myTargetDir;

    static LightProjectDescriptor getDescriptor() {
        LightProjectDescriptor descriptor = new DefaultLightProjectDescriptor();
        return descriptor;
    }

    @BeforeAll
    static void beforeAll(IdeaProjectTestFixture fixture) throws Exception {
        myFixture = IdeaTestFixtureFactory.getFixtureFactory().createCodeInsightFixture(fixture, new LightTempDirTestFixtureImpl(true));

        myFixture.setUp();
        myTestDataPath = Paths.get(".", "testRun", "it", NodeDecorationIT.class.getSimpleName());
        initGit(myTestDataPath);
        myFixture.setTestDataPath(myTestDataPath.toAbsolutePath().toString());
        myModule = myFixture.getModule();
        myProject = myFixture.getProject();
        myTargetDir = WriteCommandAction.runWriteCommandAction(myProject,
                (Computable<VirtualFile>) () -> myFixture.copyDirectoryToProject(".", "."));
    }

    private static void initGit(Path testDataDir) throws Exception {
        Git git = Git.init().setDirectory(testDataDir.toFile()).call();
        Files.write(testDataDir.resolve("file.txt"), Arrays.asList("abc"), Charsets.UTF_8);
        git.add().addFilepattern("file.txt").call();
        git.commit().setMessage("Initial commit").call();
    }

    @AfterAll
    static void afterAll() throws Exception {
        try {
            myFixture.tearDown();
        }
        finally {
            myFixture = null;
            myModule = null;
        }
    }

    @Test
    void test() {
        WriteCommandAction.runWriteCommandAction(myProject, () -> {
            ProjectLevelVcsManager vcsManager = ProjectLevelVcsManager.getInstance(myProject);
            vcsManager.setDirectoryMapping(myTargetDir.getUrl(), GitVcs.NAME);
        });
        GitRepository repository = GitUtil.getRepositoryManager(myProject).getRepositoryForRoot(myTargetDir);
        assertThat(repository).isNotNull();
        PerRepoInfoCache cache = GitToolBoxProject.getInstance(myProject).perRepoStatusCache();
        assertThat(cache.getInfo(repository).count()).isNotEmpty();
    }

    @Test
    void test2() {
        WriteCommandAction.runWriteCommandAction(myProject, () -> {
            ProjectLevelVcsManager vcsManager = ProjectLevelVcsManager.getInstance(myProject);
            VcsDirectoryMapping mapping = new VcsDirectoryMapping(myTargetDir.getUrl(), GitVcs.NAME);
            vcsManager.setDirectoryMappings(Lists.newArrayList(mapping));
        });
        Repository repository = VcsRepositoryManager.getInstance(myProject).getRepositoryForRoot(myTargetDir);
        assertThat(repository).isNotNull();
    }
}
