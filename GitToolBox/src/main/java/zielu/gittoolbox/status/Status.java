package zielu.gittoolbox.status;

public enum Status {
  SUCCESS(true),
  NO_REMOTE(true),
  CANCEL(false),
  FAILURE(false);

  private final boolean valid;

  Status(boolean valid) {
    this.valid = valid;
  }

  public boolean isValid() {
    return valid;
  }
}
