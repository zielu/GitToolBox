package zielu.gittoolbox.util;

import zielu.gittoolbox.fetch.GtFetchResult;

public class FetchResult {
  private final GtFetchResult result;

  public FetchResult(GtFetchResult result) {
    this.result = result;
  }

  public GtFetchResult result() {
    return result;
  }
}
