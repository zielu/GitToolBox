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
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.ResIcons;
import zielu.gittoolbox.config.CommitCompletionMode;
import zielu.gittoolbox.config.GitToolBoxConfig2;
import zielu.gittoolbox.config.GitToolBoxConfigForProject;
import zielu.gittoolbox.formatter.Formatted;
import zielu.gittoolbox.formatter.Formatter;
import zielu.gittoolbox.formatter.RegExpFormatter;
import zielu.gittoolbox.formatter.SimpleFormatter;
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
      setupCompletions(parameters, result);
    }
  }

  private void setupCompletions(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
    Project project = getProject(parameters);
    List<Formatter> formatters = getFormatters(project);
    Collection<Pair<String, String>> branchInfos = getBranchInfo(project);
    log.debug("Setup completions for: ", branchInfos);
    branchInfos.forEach(branchInfo -> {
      String branchName = branchInfo.getFirst();
      formatters.forEach(formatter -> addCompletion(result, formatter, branchName, branchInfo.getSecond()));
    });
  }

  private void addCompletion(CompletionResultSet result, Formatter formatter, String branchName, String repoName) {
    Formatted formatted = formatter.format(branchName);
    if (formatted.isDisplayable()) {
      result.addElement(LookupElementBuilder.create(formatted.getText())
          .withTypeText(repoName, true)
          .withIcon(getIcon(formatter)));
    } else {
      log.debug("Skipped completion: ", formatted);
    }
  }

  private Icon getIcon(Formatter formatter) {
    if (formatter instanceof RegExpFormatter) {
      return ResIcons.BranchViolet;
    } else if (formatter instanceof SimpleFormatter) {
      return ResIcons.BranchOrange;
    } else {
      return null;
    }
  }

  private List<Formatter> getFormatters(Project project) {
    return project.getComponent(CompletionService.class).getFormatters();
  }

  @NotNull
  private Project getProject(@NotNull CompletionParameters parameters) {
    return parameters.getPosition().getProject();
  }

  private Collection<Pair<String, String>> getBranchInfo(@NotNull Project project) {
    CompletionService completion = CompletionService.getInstance(project);
    return completion.getAffected().stream().map(getGitRepositoryNames()).collect(Collectors.toList());
  }

  private GitToolBoxConfigForProject getConfig(@NotNull CompletionParameters parameters) {
    Project project = getProject(parameters);
    return getConfig(project);
  }

  private GitToolBoxConfigForProject getConfig(@NotNull Project project) {
    return GitToolBoxConfigForProject.getInstance(project);
  }

  private boolean shouldComplete(@NotNull CompletionParameters parameters) {
    GitToolBoxConfigForProject projectConfig = getConfig(parameters);
    if (projectConfig.commitDialogCompletion) {
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
