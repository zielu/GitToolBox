package zielu.junit5.intellij.extension.git

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vcs.VcsDirectoryMapping
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.testFramework.PsiTestUtil
import com.intellij.testFramework.UsefulTestCase
import git4idea.GitUtil
import git4idea.GitVcs
import org.assertj.core.api.Assertions
import java.nio.file.Path
import java.nio.file.Paths

internal object GitProject {
  @JvmStatic
  fun setup(project: Project, module: Module, testDataPath: Path) {
    val root = rootFor(module)
    FileUtil.copyDir(testDataPath.toFile(), root.toFile())
    val rootVf = WriteCommandAction.runWriteCommandAction(
      project,
      Computable {
        val vf = UsefulTestCase.refreshAndFindFile(root.toFile())
        UsefulTestCase.refreshRecursively(vf)
        vf
      }
    )
    val vcsManager = ProjectLevelVcsManager.getInstance(project)
    val rootPath = rootVf.path
    vcsManager.directoryMappings = listOf(VcsDirectoryMapping(rootPath, GitVcs.NAME))
    Assertions.assertThat(LocalFileSystem.getInstance().findFileByPath(rootPath)).isNotNull
    val repository = GitUtil.getRepositoryManager(project).getRepositoryForRoot(rootVf)
    Assertions.assertThat(repository).isNotNull
    PsiTestUtil.addContentRoot(module, rootVf)
  }

  @JvmStatic
  fun rootFor(module: Module): Path {
    return Paths.get(ModuleUtil.getModuleDirPath(module))
  }
}
