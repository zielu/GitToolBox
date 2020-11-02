package zielu.gittoolbox.cache;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.vcs.log.Hash;
import git4idea.repo.GitRepository;
import java.util.List;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.status.GitAheadBehindCount;
import zielu.gittoolbox.status.GitStatusCalculator;
import zielu.gittoolbox.tag.GitTagCalculator;
import zielu.gittoolbox.util.GtUtil;
import zielu.gittoolbox.util.MemoizeSupplier;
import zielu.intellij.metrics.GtTimer;
import zielu.intellij.metrics.Metrics;

class CachedStatusCalculator {
  private final Logger log = Logger.getInstance(getClass());
  private final Supplier<Metrics> metrics;

  CachedStatusCalculator(Supplier<Metrics> metrics) {
    this.metrics = new MemoizeSupplier<>(metrics);
  }

  public RepoInfo update(@NotNull GitRepository repo, @NotNull GitStatusCalculator calculator,
                         @NotNull RepoStatus currentStatus) {
    return updateStatus(repo, calculator, currentStatus);
  }

  private RepoInfo updateStatus(@NotNull GitRepository repo, @NotNull GitStatusCalculator calculator,
                            RepoStatus status) {
    GtTimer statusUpdateTimer = metrics.get().timer("status-update");
    GitAheadBehindCount count = statusUpdateTimer
        .timeSupplier(() -> calculator.aheadBehindStatus(repo, status.localHash(), status.parentHash()));
    log.debug("Calculated status [", GtUtil.name(repo), "]: ", count);
    if (!status.sameHashes(count)) {
      log.warn("Hash mismatch between count and status: " + count + " <> " + status);
    }
    GtTimer tagsUpdateTimer = metrics.get().timer("tags-update");
    List<String> tags = tagsUpdateTimer.timeSupplier(() -> calculateTags(repo, status.localHash()));
    return RepoInfo.create(status, count, tags);
  }

  private List<String> calculateTags(@NotNull GitRepository repository, @Nullable Hash localHash) {
    GitTagCalculator tagCalculator = GitTagCalculator.create(repository.getProject());
    if (localHash == null) {
      return tagCalculator.tagsForHead(repository.getRoot());
    } else {
      return tagCalculator.tagsForCommit(repository.getRoot(), localHash);
    }
  }
}
