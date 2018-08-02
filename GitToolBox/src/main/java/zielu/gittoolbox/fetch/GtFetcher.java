package zielu.gittoolbox.fetch;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.MoreExecutors;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.DoubleAdder;
import jodd.util.StringBand;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.compat.Notifier;
import zielu.gittoolbox.metrics.Metrics;
import zielu.gittoolbox.metrics.MetricsHost;
import zielu.gittoolbox.util.ConcurrentUtil;
import zielu.gittoolbox.util.FetchResult;
import zielu.gittoolbox.util.FetchResultsPerRoot;
import zielu.gittoolbox.util.Html;

public class GtFetcher {
  private final Logger log = Logger.getInstance(getClass());

  private final Project project;
  private final ProgressIndicator progressIndicator;
  private final Executor executor;
  private final GitFetcher fetcher;
  private final GitRepositoryManager repositoryManager;

  private GtFetcher(Project project, ProgressIndicator progress, Builder builder) {
    this.project = project;
    progressIndicator = progress;
    this.executor = builder.executor;
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
    Metrics metrics = MetricsHost.project(this.project);
    return metrics.timer("fetch-roots").timeSupplier(() -> doFetchRoots(repositories));
  }

  private ImmutableCollection<GitRepository> doFetchRoots(@NotNull Collection<GitRepository> repositories) {
    Map<VirtualFile, String> additionalInfos = new ConcurrentHashMap<>(repositories.size());
    FetchResultsPerRoot errorsPerRoot = new FetchResultsPerRoot();
    ImmutableList.Builder<GitRepository> resultBuilder = ImmutableList.builder();
    Progress progress = new Progress(1f / repositories.size());
    progressIndicator.setIndeterminate(false);
    List<CompletableFuture<?>> fetches = new ArrayList<>(repositories.size());
    for (GitRepository repository : repositories) {
      CompletableFuture<Void> fetch = fetchRepository(repository).thenApply(fetchDone -> {
        progressIndicator.setFraction(progress.increment());
        log.debug("Fetched ", repository, ": success=", fetchDone.isSuccess(),
            ", error=", fetchDone.isError());
        return fetchDone;
      }).thenAccept(fetchDone -> {
        fetchDone.getAdditionalInfo().ifPresent(ai -> additionalInfos.put(fetchDone.getRoot(), ai));
        if (fetchDone.isSuccess()) {
          resultBuilder.add(fetchDone.repository);
        } else {
          errorsPerRoot.add(fetchDone.repository, new FetchResult(fetchDone.result, fetcher.getErrors()));
        }
      });
      fetches.add(fetch);
    }
    CompletableFuture<ImmutableList<GitRepository>> repos = ConcurrentUtil.allOf(fetches)
        .thenApply(unused -> {
          errorsPerRoot.showProblems(Notifier.getInstance(project));
          showAdditionalInfos(additionalInfos);
          return resultBuilder.build();
        });
    try {
      return repos.get();
    } catch (InterruptedException e) {
      log.error(e);
      Thread.currentThread().interrupt();
    } catch (ExecutionException e) {
      log.error(e);
    }
    return ImmutableList.of();
  }

  private CompletableFuture<FetchDone> fetchRepository(GitRepository repository) {
    return CompletableFuture.supplyAsync(() -> {
      log.debug("Fetching ", repository);
      Metrics metrics = MetricsHost.project(this.project);
      GitFetchResult fetch = metrics.timer("fetch-root").timeSupplier(() -> fetcher.fetch(repository));
      return new FetchDone(repository, fetch);
    }, executor);
  }

  private void showAdditionalInfos(Map<VirtualFile, String> additionalInfos) {
    String additionalInfo = makeAdditionalInfoByRoot(additionalInfos);
    if (!StringUtil.isEmptyOrSpaces(additionalInfo)) {
      Notifier.getInstance(project).fetchInfo("Fetch details", additionalInfo);
    }
  }

  public static class Builder {
    private boolean fetchAll = false;
    private Executor executor = MoreExecutors.directExecutor();

    private Builder() {
    }

    public Builder fetchAll() {
      fetchAll = true;
      return this;
    }

    public Builder withExecutor(@NotNull Executor executor) {
      this.executor = executor;
      return this;
    }

    public GtFetcher build(@NotNull Project project, @NotNull ProgressIndicator progress) {
      return new GtFetcher(project, progress, this);
    }
  }

  private final class Progress {
    private final DoubleAdder current = new DoubleAdder();
    private final double fraction;

    private Progress(double fraction) {
      this.fraction = fraction;
    }

    private double increment() {
      current.add(fraction);
      return current.doubleValue();
    }
  }

  private final class FetchDone {
    private final GitRepository repository;
    private final GitFetchResult result;

    FetchDone(GitRepository repository, GitFetchResult result) {
      this.repository = repository;
      this.result = result;
    }

    boolean isSuccess() {
      return result.isSuccess();
    }

    boolean isError() {
      return result.isError();
    }

    Optional<String> getAdditionalInfo() {
      return Optional.of(result.getAdditionalInfo()).filter(value -> !StringUtil.isEmptyOrSpaces(value));
    }

    VirtualFile getRoot() {
      return repository.getRoot();
    }
  }
}
