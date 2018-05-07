package zielu.gittoolbox.util.diagnostics;

import com.google.common.base.Stopwatch;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import jodd.util.StringBand;

class PerfWatchImpl implements PerfWatch {
  private final String message;
  private final boolean enabled;
  private final Stopwatch stopwatch;

  PerfWatchImpl(String message) {
    enabled = PerfUtil.isEnabled();
    if (enabled) {
      this.message = message;
      stopwatch = Stopwatch.createUnstarted();
    } else {
      this.message = "";
      stopwatch = null;
    }
  }

  @Override
  public PerfWatch start() {
    if (enabled && !stopwatch.isRunning()) {
      stopwatch.start();
    }
    return this;
  }

  @Override
  public PerfWatch elapsed(String message, Object... rest) {
    if (enabled) {
      StringBand messageToPrint = new StringBand(this.message).append("|Elapsed/").append(message);
      Arrays.stream(rest).forEach(messageToPrint::append);
      print(messageToPrint, stopwatch.elapsed(TimeUnit.MILLISECONDS));
    }
    return this;
  }

  @Override
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
      PerfUtil.log(messageToLog);
    }
  }
}
