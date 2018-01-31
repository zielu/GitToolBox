package zielu.gittoolbox.fetch;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.DumbProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsImplUtil;
import git4idea.GitUtil;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import git4idea.update.GitFetchResult;
import git4idea.update.GitFetcher;
import java.util.Collection;
import java.util.Map;
import jodd.util.StringBand;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.compat.Notifier;
import zielu.gittoolbox.util.FetchResult;
import zielu.gittoolbox.util.FetchResultsPerRoot;
import zielu.gittoolbox.util.GtUtil;
import zielu.gittoolbox.util.Html;

public class GtFetcher {
  private final Logger log = Logger.getInstance(getClass());

  private final Project project;
  private final ProgressIndicator progressIndicator;
  private final GitFetcher fetcher;
  private final GitRepositoryManager repositoryManager;

  private GtFetcher(@NotNull Project project, @NotNull ProgressIndicator progress, Builder builder) {
    this.project = Preconditions.checkNotNull(project, "Null Project");
    progressIndicator = progress;
    fetcher = new GitFetcher(this.project, DumbProgressIndicator.INSTANCE, builder.fetchAll);
    repositoryManager = GitUtil.getRepositoryManager(this.project);
  }

  public static Builder builder() {
    return new Builder();
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

  public ImmutableCollection<GitRepository> fetchRoots(@NotNull Collection<GitRepository> repositories) {
    final float fraction = 1f / repositories.size();
    Map<VirtualFile, String> additionalInfos = Maps.newHashMapWithExpectedSize(repositories.size());
    FetchResultsPerRoot errorsPerRoot = new FetchResultsPerRoot();
    ImmutableList.Builder<GitRepository> resultBuilder = ImmutableList.builder();
    float done = fraction;
    for (GitRepository repository : repositories) {
      if (progressIndicator.isCanceled()) {
        break;
      }
      log.debug("Fetching ", repository);
      progressIndicator.startNonCancelableSection();
      progressIndicator.setText2(GtUtil.name(repository));
      GitFetchResult result = fetcher.fetch(repository);
      progressIndicator.setFraction(done);
      progressIndicator.finishNonCancelableSection();
      log.debug("Fetched ", repository, ": success=", result.isSuccess(), ", error=", result.isError());
      done += fraction;
      String ai = result.getAdditionalInfo();
      if (!StringUtil.isEmptyOrSpaces(ai)) {
        additionalInfos.put(repository.getRoot(), ai);
      }
      if (result.isSuccess()) {
        resultBuilder.add(repository);
      } else {
        errorsPerRoot.add(repository, new FetchResult(result, fetcher.getErrors()));
      }
    }

    errorsPerRoot.showProblems(Notifier.getInstance(project));
    showAdditionalInfos(additionalInfos);

    return resultBuilder.build();
  }

  private void showAdditionalInfos(Map<VirtualFile, String> additionalInfos) {
    String additionalInfo = makeAdditionalInfoByRoot(additionalInfos);
    if (!StringUtil.isEmptyOrSpaces(additionalInfo)) {
      Notifier.getInstance(project).fetchInfo("Fetch details", additionalInfo);
    }
  }

  public static class Builder {
    private boolean fetchAll = false;

    private Builder() {
    }

    public Builder fetchAll() {
      fetchAll = true;
      return this;
    }

    public GtFetcher build(@NotNull Project project, @NotNull ProgressIndicator progress) {
      return new GtFetcher(project, progress, this);
    }
  }
}
