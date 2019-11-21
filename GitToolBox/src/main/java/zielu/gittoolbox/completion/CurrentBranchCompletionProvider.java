package zielu.gittoolbox.completion;

import com.google.common.collect.ImmutableMap;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.util.BooleanFunction;
import com.intellij.util.ProcessingContext;
import git4idea.branch.GitBranchUtil;
import git4idea.repo.GitRepository;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.config.CommitCompletionMode;
import zielu.gittoolbox.config.GitToolBoxConfig2;
import zielu.gittoolbox.config.GitToolBoxConfigPrj;
import zielu.gittoolbox.formatter.Formatted;
import zielu.gittoolbox.formatter.Formatter;
import zielu.gittoolbox.util.GtUtil;

class CurrentBranchCompletionProvider extends CompletionProvider<CompletionParameters> {
  private static final Map<CommitCompletionMode, BooleanFunction<CompletionParameters>> MODE_HANDLERS = ImmutableMap
      .<CommitCompletionMode, BooleanFunction<CompletionParameters>>builder()
      .put(CommitCompletionMode.AUTOMATIC, parameters -> true)
      .put(CommitCompletionMode.ON_DEMAND, parameters -> !parameters.isAutoPopup())
      .build();
  private final Logger log = Logger.getInstance(getClass());

  @Override
  protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context,
                                @NotNull CompletionResultSet result) {
    if (shouldComplete(parameters)) {
      setupCompletions(getProject(parameters), result);
    }
  }

  private void setupCompletions(@NotNull Project project, @NotNull CompletionResultSet result) {
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
          .withIcon(formatter.getIconHandle().getIcon()));
    } else {
      log.debug("Skipped completion: ", formatted);
    }
  }

  @NotNull
  private Project getProject(@NotNull CompletionParameters parameters) {
    return parameters.getPosition().getProject();
  }

  private Collection<Pair<String, String>> getBranchInfo(@NotNull CompletionService completionService) {
    return completionService.getAffected().stream().map(getGitRepositoryNames()).collect(Collectors.toList());
  }

  private GitToolBoxConfigPrj getConfig(@NotNull CompletionParameters parameters) {
    Project project = getProject(parameters);
    return getConfig(project);
  }

  private GitToolBoxConfigPrj getConfig(@NotNull Project project) {
    return GitToolBoxConfigPrj.getInstance(project);
  }

  private boolean shouldComplete(@NotNull CompletionParameters parameters) {
    GitToolBoxConfigPrj projectConfig = getConfig(parameters);
    if (projectConfig.getCommitDialogCompletion()) {
      CommitCompletionMode mode = GitToolBoxConfig2.getInstance().commitDialogCompletionMode;
      BooleanFunction<CompletionParameters> modeHandler = MODE_HANDLERS.getOrDefault(mode, params -> true);
      return modeHandler.fun(parameters);
    }
    return false;
  }

  @NotNull
  private static Function<GitRepository, Pair<String, String>> getGitRepositoryNames() {
    return repo -> Pair.create(GitBranchUtil.getDisplayableBranchText(repo), GtUtil.name(repo));
  }
}
