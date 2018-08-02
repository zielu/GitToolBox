package zielu.gittoolbox.util;

import com.intellij.openapi.diagnostic.Logger;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;

public class ConcurrentUtil {
  private static final Logger LOG = Logger.getInstance(ConcurrentUtil.class);

  private ConcurrentUtil() {
    throw new IllegalStateException();
  }

  public static void shutdown(ExecutorService executor) {
    executor.shutdownNow().forEach(notStarted -> LOG.info("Task " + notStarted + " was never started"));
  }

  public static CompletableFuture<Void> allOf(Collection<CompletableFuture<?>> stages) {
    return CompletableFuture.allOf(stages.toArray(new CompletableFuture[0]));
  }
}
