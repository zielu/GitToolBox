package zielu.gittoolbox.util.diagnostics;

class NoopPerfWatch implements PerfWatch {
  static final PerfWatch INSTANCE = new NoopPerfWatch();

  private NoopPerfWatch() {
  }

  @Override
  public PerfWatch start() {
    return this;
  }

  @Override
  public PerfWatch elapsed(String message, Object... rest) {
    return this;
  }

  @Override
  public void finish() {
  }
}
