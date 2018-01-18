package zielu.gittoolbox.util.diagnostics;

import java.util.Arrays;
import jodd.util.StringBand;

public interface PerfWatch {
  static PerfWatch create(String message, Object... rest) {
    if (LogWatchUtil.isPerfEnabled()) {
      StringBand initialMessage = new StringBand(message);
      Arrays.stream(rest).forEach(initialMessage::append);
      return new PerfWatchImpl(initialMessage.toString());
    } else {
      return NoopPerfWatch.INSTANCE;
    }
  }

  static PerfWatch createStarted(String message, Object... rest) {
    return create(message, rest).start();
  }

  PerfWatch start();

  PerfWatch elapsed(String message, Object... rest);

  void finish();
}
