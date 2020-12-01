package zielu.junit5.intellij.extension.git

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vcs.ProjectLevelVcsManager
import com.intellij.openapi.vcs.VcsDirectoryMapping
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.PsiTestUtil
import com.intellij.testFramework.UsefulTestCase
import git4idea.GitUtil
import git4idea.GitVcs
import org.assertj.core.api.Assertions
import java.nio.file.Path

internal object GitProject {
  @JvmStatic
  fun setup(project: Project, module: Module, testDataPath: Path) {
    val root: VirtualFile = rootFor(module)
    FileUtil.copyDir(testDataPath.toFile(), VfsUtil.virtualToIoFile(root))
    UsefulTestCase.refreshRecursively(root)
    val vcsManager = ProjectLevelVcsManager.getInstance(project)
    val rootPath = root.path
    vcsManager.directoryMappings = listOf(VcsDirectoryMapping(rootPath, GitVcs.NAME))
    Assertions.assertThat(LocalFileSystem.getInstance().findFileByPath(rootPath)).isNotNull
    val repository = GitUtil.getRepositoryManager(project).getRepositoryForRoot(root)
    Assertions.assertThat(repository).isNotNull
    PsiTestUtil.addContentRoot(module, root)
  }

  @JvmStatic
  fun rootFor(module: Module): VirtualFile {
    return module.moduleFile!!.parent
  }
}
