package zielu.gittoolbox.fetch;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsImplUtil;
import git4idea.GitUtil;
import git4idea.repo.GitRepositoryManager;
import java.util.Map;
import jodd.util.StringBand;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.compat.Notifier;
import zielu.gittoolbox.ui.util.AppUtil;
import zielu.gittoolbox.util.FetchResultsPerRoot;
import zielu.gittoolbox.util.Html;

class GtFetcherUi {
  private final Project project;
  private final GitRepositoryManager repositoryManager;

  GtFetcherUi(@NotNull Project project) {
    this.project = project;
    repositoryManager = GitUtil.getRepositoryManager(project);
  }

  void showProblems(@NotNull FetchResultsPerRoot results) {
    invokeLaterIfNeeded(() -> results.showProblems(Notifier.getInstance(project)));
  }

  void showAdditionalInfo(@NotNull Map<VirtualFile, String> additionalInfos) {
    invokeLaterIfNeeded(() -> showAdditionalInfoInternal(additionalInfos));
  }

  void invokeLaterIfNeeded(Runnable task) {
    AppUtil.invokeLaterIfNeeded(task);
  }

  private void showAdditionalInfoInternal(@NotNull Map<VirtualFile, String> additionalInfos) {
    String additionalInfo = makeAdditionalInfoByRoot(additionalInfos);
    if (!StringUtil.isEmptyOrSpaces(additionalInfo)) {
      Notifier.getInstance(project).fetchInfo("Fetch details", additionalInfo);
    }
  }

  @NotNull
  private String makeAdditionalInfoByRoot(@NotNull Map<VirtualFile, String> additionalInfo) {
    if (additionalInfo.isEmpty()) {
      return "";
    }
    StringBand info = new StringBand();
    if (repositoryManager.moreThanOneRoot()) {
      for (Map.Entry<VirtualFile, String> entry : additionalInfo.entrySet()) {
        info.append(entry.getValue()).append(" in ").append(VcsImplUtil.getShortVcsRootName(project, entry.getKey()))
            .append(Html.BR);
      }
    } else {
      info.append(additionalInfo.values().iterator().next());
    }
    return info.toString();
  }
}
