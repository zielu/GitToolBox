package zielu.gittoolbox.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiPlainText;
import com.intellij.util.ProcessingContext;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import git4idea.branch.GitBranchUtil;
import git4idea.repo.GitRepository;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.swing.Icon;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.ResIcons;
import zielu.gittoolbox.config.GitToolBoxConfigForProject;
import zielu.gittoolbox.formatter.Formatted;
import zielu.gittoolbox.formatter.Formatter;
import zielu.gittoolbox.formatter.RegExpFormatter;
import zielu.gittoolbox.formatter.SimpleFormatter;
import zielu.gittoolbox.util.GtUtil;

public class CurrentBranchCompletionContributor extends CompletionContributor {
  public CurrentBranchCompletionContributor() {
    extend(CompletionType.BASIC, PlatformPatterns.psiElement(PsiPlainText.class), new Completer());
  }

  private static class Completer extends CompletionProvider<CompletionParameters> {

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context,
                                  @NotNull CompletionResultSet result) {
      if (shouldComplete(parameters)) {
        setupCompletions(parameters, result);
      }
    }

    private void setupCompletions(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
      Project project = getProject(parameters);
      List<Formatter> formatters = getFormatters(project);
      getBranchInfo(project).forEach(branchInfo -> {
        String branchName = branchInfo.getFirst();
        formatters.forEach(formatter -> addCompletion(result, formatter, branchName, branchInfo.getSecond()));
      });
    }

    private void addCompletion(CompletionResultSet result, Formatter formatter, String branchName, String repoName) {
      Formatted formatted = formatter.format(branchName);
      if (formatted.matches && StringUtils.isNotBlank(formatted.text)) {
        result.addElement(LookupElementBuilder.create(formatted.text)
            .withTypeText(repoName, true)
            .withIcon(getIcon(formatter)));
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
      return project.getComponent(GitToolBoxCompletionProject.class).getFormatters();
    }

    @SuppressFBWarnings({"NP_NULL_ON_SOME_PATH"})
    @NotNull
    private Project getProject(@NotNull CompletionParameters parameters) {
      return parameters.getPosition().getProject();
    }

    private Collection<Pair<String, String>> getBranchInfo(@NotNull Project project) {
      GitToolBoxCompletionProject completion = GitToolBoxCompletionProject.getInstance(project);
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
      GitToolBoxConfigForProject config = getConfig(parameters);
      return config.commitDialogCompletion && !parameters.getEditor().isOneLineMode();
    }

    @NotNull
    private static Function<GitRepository, Pair<String, String>> getGitRepositoryNames() {
      return repo -> Pair.create(GitBranchUtil.getDisplayableBranchText(repo), GtUtil.name(repo));
    }
  }
}
