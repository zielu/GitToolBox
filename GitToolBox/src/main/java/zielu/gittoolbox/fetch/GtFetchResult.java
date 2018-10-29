package zielu.gittoolbox.fetch;

import java.util.Optional;

public interface GtFetchResult {
  boolean isSuccess();

  boolean isError();

  boolean isCancelled();

  boolean isNotAuthorized();

  Optional<String> getAdditionalInfo();
}
