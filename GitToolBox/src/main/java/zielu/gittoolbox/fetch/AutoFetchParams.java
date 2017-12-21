package zielu.gittoolbox.fetch;

public final class AutoFetchParams {
  public static final int DEFAULT_INTERVAL_MINUTES = 15;
  public static final int INTERVAL_MIN_MINUTES = 5;
  public static final int INTERVAL_MAX_MINUTES = 180;

  private AutoFetchParams() {
    throw new IllegalStateException();
  }
}
