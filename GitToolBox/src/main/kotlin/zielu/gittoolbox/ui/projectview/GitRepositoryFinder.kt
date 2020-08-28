package zielu.gittoolbox.ui.projectview

import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.impl.ProjectRootsUtil
import com.intellij.ide.projectView.impl.nodes.AbstractModuleNode
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import git4idea.repo.GitRepository
import zielu.gittoolbox.cache.VirtualFileRepoCache
import zielu.gittoolbox.extension.projectview.ViewModuleNodeChildExtension
import zielu.gittoolbox.extension.projectview.ViewModuleNodeParentExtension
import zielu.gittoolbox.extension.projectview.ViewPsiDirectoryNodeExtension
import zielu.intellij.java.singleOrNull
import java.util.function.BiFunction

internal class GitRepositoryFinder {
  private val viewModuleParentExtension = ViewModuleNodeParentExtension()
  private val viewModuleChildExtension = ViewModuleNodeChildExtension()
  private val viewPsiDirectoryNodeExtensions = ViewPsiDirectoryNodeExtension()

  fun getRepoFor(node: ProjectViewNode<*>): GitRepository? {
    var repository: GitRepository? = null
    if (isModuleContentRootNode(node)) {
      repository = findForModuleContentRoot(node)
      log.debug("Repo for module content root ", node, IS_PART, repository)
    } else if (isDirectoryNode(node)) {
      repository = findForDirectory(node)
      if (repository == null) {
        repository = viewPsiDirectoryNodeExtensions.findForDirectory(node)
      }
      if (repository == null && log.isTraceEnabled) {
        log.trace(
          "Unsupported directory $node, type=${node.javaClass.simpleName}, name=${node.name}" +
            ", title=${node.title}, parent=${node.parent?.javaClass?.simpleName}"
        )
      } else {
        log.debug("Repo for dir ", node, IS_PART, repository)
      }
    } else if (isChildOfViewModuleNode(node)) {
      repository = findForChildOfViewModuleNode(node)
      log.debug("Repo for child of view node ", node, IS_PART, repository)
    } else if (log.isTraceEnabled) {
      log.trace(
        "Unsupported node $node, type=${node.javaClass.simpleName}, name=${node.name}" +
          ", title=${node.title}, parent=${node.parent?.javaClass?.simpleName}"
      )
    }
    return repository
  }

  private fun findForModuleContentRoot(node: ProjectViewNode<*>): GitRepository? {
    return findRepo(node, BiFunction { obj: VirtualFileRepoCache, dir: VirtualFile? -> obj.getRepoForDir(dir!!) })
  }

  private fun findForDirectory(node: ProjectViewNode<*>): GitRepository? {
    return findRepo(node, BiFunction { obj: VirtualFileRepoCache, root: VirtualFile? -> obj.getRepoForRoot(root!!) })
  }

  private fun findRepo(
    node: ProjectViewNode<*>,
    finder: BiFunction<VirtualFileRepoCache, VirtualFile, GitRepository?>
  ): GitRepository? {
    return node.virtualFile?.let {
      val cache = VirtualFileRepoCache.getInstance(node.project!!)
      return finder.apply(cache, it)
    }
  }

  private fun isDirectoryNode(node: ProjectViewNode<*>): Boolean {
    return node is PsiDirectoryNode
  }

  private fun isModuleContentRootNode(node: ProjectViewNode<*>): Boolean {
    val file = node.virtualFile
    if (file != null && file.isDirectory) {
      log.debug("node ", node.javaClass.simpleName, " is dir ", file, " module root")
      return isModuleContentRoot(file, node.project!!)
    }
    return false
  }

  private fun isModuleContentRoot(file: VirtualFile, project: Project): Boolean {
    return ProjectRootsUtil.isModuleContentRoot(file, project)
  }

  private fun isChildOfViewModuleNode(node: ProjectViewNode<*>): Boolean {
    return viewModuleChildExtension.hasChildOfType(node.javaClass) && node.parent?.let {
      viewModuleParentExtension.hasParentOfType(it.javaClass)
    } ?: false
  }

  private fun findForChildOfViewModuleNode(node: ProjectViewNode<*>): GitRepository? {
    val parentModuleNode = node.parent as AbstractModuleNode
    val cache = VirtualFileRepoCache.getInstance(node.project!!)
    return parentModuleNode.roots.stream().map { cache.getRepoForDir(it) }.distinct().singleOrNull()
  }

  private companion object {
    private const val IS_PART = " is: "
    private val log = Logger.getInstance(GitRepositoryFinder::class.java)
  }
}
