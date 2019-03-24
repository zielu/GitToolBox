package zielu.gittoolbox.fetch;

import com.intellij.openapi.project.Project;
import git4idea.repo.GitRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.config.GitToolBoxConfigForProject;

class AutoFetchExclusions {
  private final Supplier<Set<String>> exclusionsProvider;

  AutoFetchExclusions(@NotNull Project project) {
    this.exclusionsProvider = () -> projectExclusionsProvider(project);
  }

  private Set<String> projectExclusionsProvider(@NotNull Project project) {
    GitToolBoxConfigForProject config = GitToolBoxConfigForProject.getInstance(project);
    return new HashSet<>(config.autoFetchExclusions);
  }

  AutoFetchExclusions(@NotNull Supplier<Set<String>> exclusionsProvider) {
    this.exclusionsProvider = exclusionsProvider;
  }

  List<GitRepository> apply(List<GitRepository> repositories) {
    return repositories.stream().filter(rootExcluded(exclusionsProvider.get()).negate()).collect(Collectors.toList());
  }

  boolean isAllowed(GitRepository repository) {
    return rootExcluded(exclusionsProvider.get()).negate().test(repository);
  }

  private Predicate<GitRepository> rootExcluded(Set<String> exclusions) {
    return repo -> exclusions.contains(repo.getRoot().getUrl());
  }
}
