package zielu.gittoolbox.ui.statusbar;

import com.intellij.openapi.project.Project;
import com.intellij.util.containers.hash.LinkedHashMap;
import com.intellij.util.text.DateFormatUtil;
import git4idea.repo.GitRepository;
import git4idea.util.GitUIUtil;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import jodd.util.StringBand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.cache.RepoInfo;
import zielu.gittoolbox.status.GitAheadBehindCount;
import zielu.gittoolbox.ui.StatusText;
import zielu.gittoolbox.util.GtUtil;
import zielu.gittoolbox.util.Html;

class StatusToolTip {
  private final StatusToolTipLocalGateway gateway;
  private GitRepository currentRepository;
  private String currentStatusText;

  StatusToolTip(@NotNull Project project) {
    this.gateway = new StatusToolTipLocalGateway(project);
  }

  @Nullable
  String getText() {
    if (currentRepository != null) {
      return prepareToolTip();
    } else {
      return null;
    }
  }

  private String prepareToolTip() {
    StringBand infoPart = prepareInfoToolTipPart();
    if (infoPart.length() > 0) {
      infoPart.append(Html.BRX);
    }
    if (currentStatusText == null) {
      currentStatusText = prepareStatusTooltip();
    }
    infoPart.append(currentStatusText);
    return infoPart.toString();
  }

  private String prepareStatusTooltip() {
    StringBand infoPart = new StringBand();
    Collection<GitRepository> repositories = gateway.getRepositories();
    if (repositories.size() == 1) {
      RepoInfo info = gateway.getRepoInfo(currentRepository);
      info.maybeCount().map(StatusText::formatToolTip).ifPresent(infoPart::append);
    } else if (repositories.size() > 2) {
      prepareMultiRepoTooltip(infoPart, repositories);
    }
    return infoPart.toString();
  }


  private void prepareMultiRepoTooltip(StringBand infoPart, Collection<GitRepository> repositories) {
    Map<GitRepository, String> statuses = new LinkedHashMap<>();
    final AtomicReference<GitRepository> currentRepo = new AtomicReference<>();
    for (GitRepository repository : GtUtil.sort(repositories)) {
      gateway.getRepoInfo(repository).maybeCount().map(StatusText::format).ifPresent(statusText -> {
        if (repository.equals(currentRepository)) {
          currentRepo.set(repository);
        }
        statuses.put(repository, statusText);
      });
    }
    if (!statuses.isEmpty()) {
      if (infoPart.length() > 0) {
        infoPart.append(Html.HRX);
      }
      infoPart.append(
          statuses.entrySet().stream().map(e -> {
            String repoStatus = GitUIUtil.bold(GtUtil.name(e.getKey())) + ": " + e.getValue();
            if (Objects.equals(e.getKey(), currentRepo.get())) {
              repoStatus = Html.underline(repoStatus);
            }
            return repoStatus;
          }).collect(Collectors.joining(Html.BRX))
      );
    }
  }

  private StringBand prepareInfoToolTipPart() {
    StringBand result = new StringBand();
    if (gateway.isAutoFetchEnabled()) {
      result.append(GitUIUtil.bold(ResBundle.message("message.autoFetch"))).append(": ");
      long lastAutoFetch = gateway.getLastAutoFetchTimestamp();
      if (lastAutoFetch != 0) {
        result.append(DateFormatUtil.formatBetweenDates(lastAutoFetch, System.currentTimeMillis()));
      } else {
        result.append(ResBundle.on());
      }
    }

    return result;
  }


  void update(@NotNull GitRepository repository, @Nullable GitAheadBehindCount aheadBehind) {
    currentRepository = repository;
    currentStatusText = null;
  }

  void clear() {
    currentRepository = null;
    currentStatusText = null;
  }
}
