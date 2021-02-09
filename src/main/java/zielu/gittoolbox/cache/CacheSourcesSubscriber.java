package zielu.gittoolbox.cache;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import git4idea.repo.GitRepository;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.config.GitToolBoxConfigPrj;
import zielu.gittoolbox.util.AppUtil;
import zielu.gittoolbox.util.GtUtil;

class CacheSourcesSubscriber {
  private final Logger log = Logger.getInstance(getClass());
  private final Project project;
  private final List<DirMappingAware> dirMappingAwares = new ArrayList<>();
  private final List<RepoChangeAware> repoChangeAwares = new ArrayList<>();

  CacheSourcesSubscriber(@NotNull Project project) {
    this.project = project;
    dirMappingAwares.add(new LazyDirMappingAware<>(() -> VirtualFileRepoCache.getInstance(project)));
    dirMappingAwares.add(new LazyDirMappingAware<>(() -> PerRepoInfoCache.getInstance(project)));
    repoChangeAwares.add(new LazyRepoChangeAware<>(() -> PerRepoInfoCache.getInstance(project)));
  }

  static CacheSourcesSubscriber getInstance(@NotNull Project project) {
    return AppUtil.getServiceInstance(project, CacheSourcesSubscriber.class);
  }

  void onRepoChanged(@NotNull GitRepository repository) {
    log.debug("Repo changed: ", repository);
    repoChangeAwares.forEach(aware -> aware.repoChanged(repository));
    log.debug("Repo changed notification done: ", repository);
  }

  void onDirMappingChanged() {
    log.info("Dir mappings changed");
    List<GitRepository> repositories = GtUtil.getRepositories(project);
    dirMappingAwares.forEach(aware -> aware.updatedRepoList(repositories));
    log.debug("Dir mappings change notification done");
  }

  void onConfigChanged(@NotNull GitToolBoxConfigPrj previous, @NotNull GitToolBoxConfigPrj current) {
    if (previous.isReferencePointForStatusChanged(current)) {
      GtUtil.getRepositories(project).forEach(repo ->
          repoChangeAwares.forEach(aware -> aware.repoChanged(repo)));
    }
  }
}
