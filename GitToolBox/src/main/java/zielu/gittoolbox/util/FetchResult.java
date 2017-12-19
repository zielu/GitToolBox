package zielu.gittoolbox.util;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import git4idea.update.GitFetchResult;

public class FetchResult {
  private final GitFetchResult result;
  private final ImmutableCollection<Exception> errors;

  public FetchResult(GitFetchResult result, Iterable<Exception> errors) {
    this.result = result;
    this.errors = ImmutableList.copyOf(errors);
  }

  public GitFetchResult result() {
    return result;
  }
}
