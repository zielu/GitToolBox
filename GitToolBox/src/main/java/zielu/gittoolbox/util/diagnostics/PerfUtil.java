package zielu.gittoolbox.util.diagnostics;

import static com.intellij.openapi.diagnostic.Logger.getInstance;

import com.intellij.openapi.diagnostic.Logger;
import jodd.util.StringBand;

public final class PerfUtil {
  private static final Logger PERF_LOGGER = getInstance("#zielu.gittoolbox.perf");

  private PerfUtil() {
    throw new IllegalStateException();
  }

  static Logger getLogger() {
    return PERF_LOGGER;
  }

  static boolean isEnabled() {
    return PERF_LOGGER.isTraceEnabled();
  }

  static void log(StringBand message) {
    if (PERF_LOGGER.isTraceEnabled()) {
      PERF_LOGGER.trace(message.toString());
    }
  }
}
