package zielu.gittoolbox.util;

import com.google.common.base.Stopwatch;
import com.intellij.openapi.diagnostic.Logger;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import jodd.util.StringBand;

public class LogWatch {
  private final Logger log = Logger.getInstance("#zielu.gittoolbox.perf");
  private final String message;
  private final boolean enabled;
  private final Stopwatch stopwatch;

  private LogWatch(String message) {
    enabled = log.isTraceEnabled();
    if (enabled) {
      this.message = message;
      stopwatch = Stopwatch.createUnstarted();
    } else {
      this.message = "";
      stopwatch = null;
    }
  }

  public static LogWatch create(String message) {
    return new LogWatch(message);
  }

  public static LogWatch createStarted(String message) {
    return create(message).start();
  }

  public LogWatch start() {
    if (enabled && !stopwatch.isRunning()) {
      stopwatch.start();
    }
    return this;
  }

  public LogWatch elapsed(String message, Object... rest) {
    if (enabled) {
      StringBand messageToPrint = new StringBand(this.message).append("|Elapsed/").append(message);
      Arrays.stream(rest).map(String::valueOf).forEach(messageToPrint::append);
      print(messageToPrint, stopwatch.elapsed(TimeUnit.MILLISECONDS));
    }
    return this;
  }

  public void finish() {
    if (enabled && stopwatch.isRunning()) {
      StringBand messageToPrint = new StringBand(message).append("|Finished");
      print(messageToPrint, stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));
    }
  }

  private void print(StringBand message, long millis) {
    if (millis > 0) {
      StringBand messageToLog = message.append(" [th:").append(Thread.currentThread().getName()).append("][ms]: ")
          .append(millis);
      log.trace(messageToLog.toString());
    }
  }
}
