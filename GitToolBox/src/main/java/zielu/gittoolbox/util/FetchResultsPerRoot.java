package zielu.gittoolbox.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import git4idea.GitVcs;
import git4idea.repo.GitRepository;
import git4idea.util.GitUIUtil;
import java.util.Map;
import java.util.Map.Entry;
import jodd.util.StringBand;
import zielu.gittoolbox.compat.Notifier;

public class FetchResultsPerRoot {
  private final Map<GitRepository, FetchResult> errorsPerRoot = Maps.newLinkedHashMap();
  private boolean anyProblems;

  public void add(GitRepository repository, FetchResult result) {
    Preconditions.checkState(errorsPerRoot.put(repository, result) == null);
    if (!result.result().isSuccess()) {
      anyProblems = true;
    }
  }

  private boolean executableValid(GitRepository repository) {
    GitVcs instance = GitVcs.getInstance(repository.getProject());
    return instance != null && instance.getExecutableValidator().isExecutableValid();
  }

  public void showProblems(Notifier notifier) {
    if (anyProblems) {
      boolean anyNotAuthorized = false;
      boolean anyError = false;
      StringBand message = new StringBand();
      for (Entry<GitRepository, FetchResult> entry : errorsPerRoot.entrySet()) {
        message.append(Html.BR);
        String boldName = GitUIUtil.bold(GtUtil.name(entry.getKey()));
        FetchResult entryResult = entry.getValue();
        if (entryResult.result().isCancelled()) {
          message.append(boldName).append(": cancelled by user").append(entryResult.result().getAdditionalInfo());
        } else if (entryResult.result().isNotAuthorized()) {
          message.append(boldName).append(": couldn't authorize").append(entryResult.result().getAdditionalInfo());
          anyNotAuthorized = true;
        } else if (entryResult.result().isError() && executableValid(entry.getKey())) {
          message.append(boldName).append(": fetch failed").append(entryResult.result().getAdditionalInfo());
          anyError = true;
        }
      }
      if (anyError || anyNotAuthorized) {
        notifier.fetchError("Fetch problems:" + Html.BR + message.toString());
      } else {
        notifier.fetchWarning("Fetch problems:", message.toString());
      }
    }
  }
}
