package zielu.gittoolbox.util.diagnostics;

class NoopLogWatch implements LogWatch {
  static final LogWatch INSTANCE = new NoopLogWatch();

  private NoopLogWatch() {
  }

  @Override
  public LogWatch start() {
    return this;
  }

  @Override
  public LogWatch elapsed(String message, Object... rest) {
    return this;
  }

  @Override
  public void finish() {
  }
}
