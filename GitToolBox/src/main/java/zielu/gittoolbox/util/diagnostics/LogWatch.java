package zielu.gittoolbox.util.diagnostics;

import java.util.Arrays;
import jodd.util.StringBand;

public interface LogWatch {
  static LogWatch create(String message, Object... rest) {
    if (LogWatchUtil.isPerfEnabled()) {
      StringBand initialMessage = new StringBand(message);
      Arrays.stream(rest).forEach(initialMessage::append);
      return new LogWatchImpl(initialMessage.toString());
    } else {
      return NoopLogWatch.INSTANCE;
    }
  }

  static LogWatch createStarted(String message, Object... rest) {
    return create(message, rest).start();
  }

  LogWatch start();

  LogWatch elapsed(String message, Object... rest);

  void finish();
}
