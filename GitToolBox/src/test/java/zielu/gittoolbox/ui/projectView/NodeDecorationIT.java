package zielu.gittoolbox.ui.projectView;

import com.google.common.base.Charsets;
import com.intellij.dvcs.repo.VcsRepositoryManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory;
import com.intellij.testFramework.fixtures.impl.LightTempDirTestFixtureImpl;
import com.intellij.vcsUtil.VcsUtil;
import git4idea.GitVcs;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.dircache.DirCache;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import zielu.intellij.junit5.ContainsTempFiles;
import zielu.intellij.junit5.IdeaLightExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

@ExtendWith(IdeaLightExtension.class)
@ContainsTempFiles
class NodeDecorationIT {
    private static Path myTestDataPath;
    private static CodeInsightTestFixture myFixture;
    private static Module myModule;
    private static Project myProject;

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
        ProjectLevelVcsManager vcsManager = ProjectLevelVcsManager.getInstance(myProject);
        /*vcsManager.setDirectoryMappings(VcsUtil.addMapping(vcsManager.getDirectoryMappings(),
                myTestDataPath.toAbsolutePath().toString(), GitVcs.NAME));*/
    }
}
