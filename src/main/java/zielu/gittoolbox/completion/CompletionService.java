package zielu.gittoolbox.completion;

import com.intellij.openapi.project.Project;
import git4idea.repo.GitRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.formatter.Formatter;
import zielu.gittoolbox.util.AppUtil;

public interface CompletionService {

  void setScopeProvider(@NotNull CompletionScopeProvider scopeProvider);

  @NotNull
  Collection<GitRepository> getAffected();

  @NotNull
  List<Formatter> getFormatters();

  static CompletionService getInstance(@NotNull Project project) {
    return AppUtil.getServiceInstance(project, CompletionService.class);
  }

  static Optional<CompletionService> getExistingInstance(@NotNull Project project) {
    return AppUtil.getExistingServiceInstance(project, CompletionService.class);
  }
}
