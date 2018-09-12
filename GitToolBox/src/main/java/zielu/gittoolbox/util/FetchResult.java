package zielu.gittoolbox.util;

import git4idea.update.GitFetchResult;

public class FetchResult {
  private final GitFetchResult result;

  public FetchResult(GitFetchResult result) {
    this.result = result;
  }

  public GitFetchResult result() {
    return result;
  }
}
