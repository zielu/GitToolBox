package zielu.gittoolbox.completion;

import com.intellij.openapi.project.Project;
import git4idea.repo.GitRepository;
import java.util.Collection;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.formatter.Formatter;

interface CompletionService {

  static CompletionService getInstance(@NotNull Project project) {
    return project.getComponent(CompletionService.class);
  }

  void setScopeProvider(@NotNull CompletionScopeProvider scopeProvider);

  @NotNull
  Collection<GitRepository> getAffected();

  @NotNull
  List<Formatter> getFormatters();
}
