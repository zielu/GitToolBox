package zielu.gittoolbox.completion;

import com.google.common.collect.ImmutableList;
import com.intellij.openapi.project.Project;
import git4idea.repo.GitRepository;
import java.io.File;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.compat.GitCompatUtil;
import zielu.gittoolbox.config.GitToolBoxConfigPrj;
import zielu.gittoolbox.formatter.Formatter;
import zielu.gittoolbox.util.GatewayBase;

class CompletionGateway extends GatewayBase {
  CompletionGateway(@NotNull Project project) {
    super(project);
  }

  @NotNull
  ImmutableList<Formatter> getFormatters() {
    return ImmutableList.copyOf(GitToolBoxConfigPrj.getInstance(project).getCompletionFormatters());
  }

  @NotNull
  Collection<GitRepository> getRepositories(@NotNull Collection<File> files) {
    return GitCompatUtil.getRepositoriesForFiles(project, files);
  }
}
