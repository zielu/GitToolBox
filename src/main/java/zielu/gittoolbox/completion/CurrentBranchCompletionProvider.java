package zielu.gittoolbox.completion;

import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import git4idea.branch.GitBranchUtil;
import git4idea.repo.GitRepository;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.formatter.Formatted;
import zielu.gittoolbox.formatter.Formatter;
import zielu.gittoolbox.util.GtUtil;

class CurrentBranchCompletionProvider extends CompletionProviderBase {
  private final Logger log = Logger.getInstance(getClass());

  protected void setupCompletions(@NotNull Project project, @NotNull CompletionResultSet result) {
    CompletionService completionService = CompletionService.getInstance(project);
    List<Formatter> formatters = completionService.getFormatters();
    Collection<Pair<String, String>> branchInfos = getBranchInfo(completionService);
    log.debug("Setup completions for: ", branchInfos);
    branchInfos.forEach(branchInfo -> {
      String branchName = branchInfo.getFirst();
      formatters.forEach(formatter -> addCompletion(result, formatter, branchName, branchInfo.getSecond()));
    });
  }

  private void addCompletion(CompletionResultSet result, Formatter formatter, String branchName, String repoName) {
    Formatted formatted = formatter.format(branchName);
    if (formatted.getDisplayable()) {
      result.addElement(LookupElementBuilder.create(formatted.getText())
          .withTypeText(repoName, true)
          .withIcon(formatter.getIcon()));
    } else {
      log.debug("Skipped completion: ", formatted);
    }
  }

  private Collection<Pair<String, String>> getBranchInfo(@NotNull CompletionService completionService) {
    return completionService.getAffected().stream().map(getGitRepositoryNames()).collect(Collectors.toList());
  }

  @NotNull
  private static Function<GitRepository, Pair<String, String>> getGitRepositoryNames() {
    return repo -> Pair.create(GitBranchUtil.getDisplayableBranchText(repo), GtUtil.name(repo));
  }
}
