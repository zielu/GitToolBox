package zielu.gittoolbox.ui.projectView;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.intellij.dvcs.repo.Repository;
import com.intellij.dvcs.repo.VcsRepositoryManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsDirectoryMapping;
import com.intellij.openapi.vfs.VirtualFile;
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
import zielu.gittoolbox.status.GitAheadBehindCount;
import zielu.junit5.intellij.ContainsTempFiles;
import zielu.junit5.intellij.IdeaHeavyExtension;
import zielu.junit5.mockito.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.fest.assertions.Assertions.assertThat;

@ExtendWith(IdeaHeavyExtension.class)
@ExtendWith(MockitoExtension.class)
@ContainsTempFiles
class NodeDecorationHeavyIT {
    private static Path myTestDataPath;
    private static VirtualFile myTargetDir;

    @BeforeAll
    static void beforeAll(Project project, Module module) throws Exception {
        myTestDataPath = Paths.get(".", "testRun", "it", NodeDecorationHeavyIT.class.getSimpleName());
        initGit(myTestDataPath);
        /*myTargetDir = WriteCommandAction.runWriteCommandAction(project,
                (Computable<VirtualFile>) () -> FileUtil.copyDir(myTestDataPath.toFile(), module.getC);*/
    }

    private static void initGit(Path testDataDir) throws Exception {
        Git git = Git.init().setDirectory(testDataDir.toFile()).call();
        Files.write(testDataDir.resolve("file.txt"), Arrays.asList("abc"), Charsets.UTF_8);
        git.add().addFilepattern("file.txt").call();
        git.commit().setMessage("Initial commit").call();
    }
}
