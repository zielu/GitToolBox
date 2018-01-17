package zielu.gittoolbox.util.diagnostics;

import static com.intellij.openapi.diagnostic.Logger.getInstance;

import com.intellij.openapi.diagnostic.Logger;
import jodd.util.StringBand;

final class LogWatchUtil {
  private static final Logger PERF_LOGGER = getInstance("#zielu.gittoolbox.perf");

  private LogWatchUtil() {
    throw new IllegalStateException();
  }

  static Logger getPerfLogger() {
    return PERF_LOGGER;
  }

  static boolean isPerfEnabled() {
    return PERF_LOGGER.isTraceEnabled();
  }

  static void log(StringBand message) {
    if (PERF_LOGGER.isTraceEnabled()) {
      PERF_LOGGER.trace(message.toString());
    }
  }
}
