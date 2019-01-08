package zielu.gittoolbox.fetch;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.MoreExecutors;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.repo.GitRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.DoubleAdder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.metrics.Metrics;
import zielu.gittoolbox.util.ConcurrentUtil;
import zielu.gittoolbox.util.FetchResult;
import zielu.gittoolbox.util.FetchResultsPerRoot;

public class GtFetcher {
  private final Logger log = Logger.getInstance(getClass());
  private final ProgressIndicator progressIndicator;
  private final Executor executor;
  private final GtFetchClient fetcher;
  private final GtFetcherUi ui;
  private final Metrics metrics;

  private GtFetcher(ProgressIndicator progress, Builder builder) {
    progressIndicator = progress;
    this.executor = Preconditions.checkNotNull(builder.executor, "Null executor");
    fetcher = Preconditions.checkNotNull(builder.client, "Null client");
    this.ui = Preconditions.checkNotNull(builder.ui, "Null ui");
    this.metrics = Preconditions.checkNotNull(builder.metrics, "Null metrics");
  }

  public static Builder builder() {
    return new Builder();
  }

  public ImmutableCollection<GitRepository> fetchRoots(@NotNull Collection<GitRepository> repositories) {
    return metrics.timer("fetch-roots-custom").timeSupplier(() -> doFetchRoots(repositories));
  }

  private ImmutableCollection<GitRepository> doFetchRoots(@NotNull Collection<GitRepository> repositories) {
    Map<VirtualFile, String> additionalInfos = new ConcurrentHashMap<>();
    FetchResultsPerRoot errorsPerRoot = new FetchResultsPerRoot();
    List<GitRepository> results = new CopyOnWriteArrayList<>();
    Progress progress = new Progress(1f / repositories.size());
    ui.invokeLaterIfNeeded(() -> progressIndicator.setIndeterminate(false));
    List<CompletableFuture<?>> fetches = new ArrayList<>(repositories.size());
    for (GitRepository repository : repositories) {
      CompletableFuture<Void> fetch = fetchRepository(repository).handleAsync((fetchDone, error) -> {
        ui.invokeLaterIfNeeded(() -> progressIndicator.setFraction(progress.increment()));
        log.debug("Fetched ", repository, ": ", fetchDone);
        if (error != null) {
          throw new RuntimeException("Repository " + repository + " fetch failed", error);
        }
        return fetchDone;
      }, executor).thenAcceptAsync(fetchDone -> {
        fetchDone.getAdditionalInfo().ifPresent(ai -> additionalInfos.put(fetchDone.getRoot(), ai));
        if (fetchDone.isSuccess()) {
          results.add(fetchDone.repository);
        } else {
          errorsPerRoot.add(fetchDone.repository, new FetchResult(fetchDone.result));
        }
      }, executor);
      fetches.add(fetch);
    }
    CompletableFuture<List<GitRepository>> repos = ConcurrentUtil.allOf(fetches)
        .handleAsync((unused, error) -> {
          ui.showProblems(errorsPerRoot);
          ui.showAdditionalInfo(additionalInfos);
          if (error != null) {
            log.warn("Some fetches failed", error);
          }
          return results;
        }, executor);
    try {
      return ImmutableList.copyOf(repos.get());
    } catch (InterruptedException e) {
      log.warn("Fetch interrupted", e);
      Thread.currentThread().interrupt();
    } catch (ExecutionException e) {
      log.warn("Fetch failed", e);
    } finally {
      ui.invokeLaterIfNeeded(() -> progressIndicator.setFraction(1));
    }
    return ImmutableList.of();
  }

  private CompletableFuture<FetchDone> fetchRepository(GitRepository repository) {
    return CompletableFuture.supplyAsync(() -> {
      log.debug("Fetching ", repository);
      GtFetchResult fetch = metrics.timer("fetch-root").timeSupplier(() -> fetcher.fetch(repository));
      return new FetchDone(repository, fetch);
    }, executor);
  }

  public static final class Builder {
    private Executor executor = MoreExecutors.directExecutor();
    private Metrics metrics;
    private GtFetcherUi ui;
    private GtFetchClient client;

    private Builder() {
    }

    public Builder withClient(@NotNull GtFetchClient client) {
      this.client = client;
      return this;
    }

    public Builder withExecutor(@NotNull Executor executor) {
      this.executor = executor;
      return this;
    }

    public Builder withMetrics(@NotNull Metrics metrics) {
      this.metrics = metrics;
      return this;
    }

    public Builder withUi(@NotNull GtFetcherUi ui) {
      this.ui = ui;
      return this;
    }

    public GtFetcher build(@NotNull ProgressIndicator progress) {
      return new GtFetcher(progress, this);
    }
  }

  private static final class Progress {
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

  private static final class FetchDone {
    private final GitRepository repository;
    private final GtFetchResult result;

    FetchDone(GitRepository repository, GtFetchResult result) {
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
      return result.getAdditionalInfo();
    }

    VirtualFile getRoot() {
      return repository.getRoot();
    }

    @Override
    public String toString() {
      return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
          .append("success", isSuccess())
          .append("error", isError())
          .build();
    }
  }
}
