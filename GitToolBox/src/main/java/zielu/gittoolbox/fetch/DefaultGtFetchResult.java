package zielu.gittoolbox.fetch;

import com.intellij.openapi.util.text.StringUtil;
import git4idea.update.GitFetchResult;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

class DefaultGtFetchResult implements GtFetchResult {
  private static final GtFetchResult SUCCESS = new GtFetchResult() {
    @Override
    public boolean isSuccess() {
      return true;
    }

    @Override
    public boolean isError() {
      return false;
    }

    @Override
    public boolean isCancelled() {
      return false;
    }

    @Override
    public boolean isNotAuthorized() {
      return false;
    }

    @Override
    public Optional<String> getAdditionalInfo() {
      return Optional.empty();
    }
  };

  private final GitFetchResult result;

  DefaultGtFetchResult(GitFetchResult result) {
    this.result = result;
  }

  @Override
  public boolean isSuccess() {
    return result.isSuccess();
  }

  @Override
  public boolean isError() {
    return result.isError();
  }

  @Override
  public boolean isCancelled() {
    return result.isCancelled();
  }

  @Override
  public boolean isNotAuthorized() {
    return result.isNotAuthorized();
  }

  @Override
  public Optional<String> getAdditionalInfo() {
    return Optional.of(result.getAdditionalInfo()).filter(value -> !StringUtil.isEmptyOrSpaces(value));
  }

  public static GtFetchResult success() {
    return SUCCESS;
  }

  public static GtFetchResult error(@NotNull Exception exception) {
    return new GtFetchResult() {
      @Override
      public boolean isSuccess() {
        return false;
      }

      @Override
      public boolean isError() {
        return true;
      }

      @Override
      public boolean isCancelled() {
        return false;
      }

      @Override
      public boolean isNotAuthorized() {
        return false;
      }

      @Override
      public Optional<String> getAdditionalInfo() {
        return Optional.empty();
      }
    };
  }
}
