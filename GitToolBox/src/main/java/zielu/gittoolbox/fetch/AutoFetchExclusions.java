package zielu.gittoolbox.fetch;

import com.intellij.openapi.project.Project;
import git4idea.repo.GitRepository;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.config.AutoFetchExclusionConfig;
import zielu.gittoolbox.config.GitToolBoxConfigForProject;

class AutoFetchExclusions {
  private final Supplier<Map<String, AutoFetchExclusionConfig>> exclusionsProvider;

  AutoFetchExclusions(@NotNull Project project) {
    this.exclusionsProvider = () -> projectExclusionsProvider(project);
  }

  private Map<String, AutoFetchExclusionConfig> projectExclusionsProvider(@NotNull Project project) {
    GitToolBoxConfigForProject config = GitToolBoxConfigForProject.getInstance(project);
    return config.autoFetchExclusionConfigs.stream()
        .collect(Collectors.toMap(AutoFetchExclusionConfig::getRepositoryRootPath, Function.identity()));
  }

  AutoFetchExclusions(@NotNull Supplier<Map<String, AutoFetchExclusionConfig>> exclusionsProvider) {
    this.exclusionsProvider = exclusionsProvider;
  }

  List<GitRepository> apply(@NotNull Collection<GitRepository> repositories) {
    Map<String, AutoFetchExclusionConfig> exclusionConfig = exclusionsProvider.get();
    return repositories.stream()
               .filter(included())
               .map(repo -> applyExclusions(repo, exclusionConfig))
               .collect(Collectors.toList());
  }

  private GitRepository applyExclusions(@NotNull GitRepository repository,
                                        Map<String, AutoFetchExclusionConfig> exclusionConfig) {
    AutoFetchExclusionConfig exclusion = exclusionConfig.get(buildConfigKey(repository));
    if (exclusion != null) {
      return new RemoteFilteredRepository(repository, exclusion);
    } else {
      return repository;
    }
  }

  boolean isAllowed(GitRepository repository) {
    return included().test(repository);
  }

  private Predicate<GitRepository> included() {
    return excluded().negate();
  }

  private Predicate<GitRepository> excluded() {
    Map<String, AutoFetchExclusionConfig> exclusionConfig = exclusionsProvider.get();
    return rootExcluded(exclusionConfig.keySet()).and(noSpecificRemotes(exclusionConfig));
  }

  private Predicate<GitRepository> rootExcluded(Set<String> exclusions) {
    return repo -> exclusions.contains(buildConfigKey(repo));
  }

  private String buildConfigKey(GitRepository repository) {
    return repository.getRoot().getUrl();
  }

  private Predicate<GitRepository> noSpecificRemotes(Map<String, AutoFetchExclusionConfig> exclusionConfig) {
    return repo -> {
      AutoFetchExclusionConfig config = exclusionConfig.get(buildConfigKey(repo));
      if (config != null) {
        return config.noRemotes();
      }
      return true;
    };
  }
}
