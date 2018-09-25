package zielu.gittoolbox.ui.projectview;

import com.google.common.collect.ImmutableSet;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.impl.ProjectRootsUtil;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.repo.GitRepository;
import java.util.Set;
import java.util.function.BiFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.cache.VirtualFileRepoCache;

public class GitRepositoryFinder {
  private static final Set<String> MODULE_NODE_CLASS_NAMES = ImmutableSet.of(
      "GradleModuleDirectoryNode"
  );
  private final Logger log = Logger.getInstance(getClass());

  @Nullable
  public GitRepository getRepoFor(ProjectViewNode node) {
    GitRepository repository = null;
    if (isModuleNode(node)) {
      repository = findForModule(node);
      log.debug("Repo for module ", node, " is: ", repository);
    } else if (isDirectoryNode(node)) {
      repository = findForDirectory(node);
      log.debug("Repo for dir ", node, " is: ", repository);
    }
    return repository;
  }

  private GitRepository findForModule(ProjectViewNode node) {
    return findRepo(node, VirtualFileRepoCache::getRepoForDir);
  }

  private GitRepository findForDirectory(ProjectViewNode node) {
    return findRepo(node, VirtualFileRepoCache::getRepoForRoot);
  }

  private GitRepository findRepo(ProjectViewNode node,
                                 BiFunction<VirtualFileRepoCache, VirtualFile, GitRepository> finder) {
    Project project = node.getProject();
    VirtualFile file = node.getVirtualFile();
    if (project != null && file != null) {
      VirtualFileRepoCache cache = VirtualFileRepoCache.getInstance(project);
      return finder.apply(cache, file);
    }
    return null;
  }

  private boolean isDirectoryNode(ProjectViewNode node) {
    return node instanceof PsiDirectoryNode;
  }

  private boolean isModuleNode(ProjectViewNode node) {
    VirtualFile file = node.getVirtualFile();
    Project project = node.getProject();
    if (file != null && project != null && file.isDirectory()) {
      return isModuleContentRoot(file, project) || isModuleNodeClass(node);
    }
    return false;
  }

  private boolean isModuleContentRoot(@NotNull VirtualFile file, @NotNull Project project) {
    return ProjectRootsUtil.isModuleContentRoot(file, project);
  }

  private boolean isModuleNodeClass(ProjectViewNode node) {
    return MODULE_NODE_CLASS_NAMES.contains(node.getClass().getSimpleName());
  }
}
