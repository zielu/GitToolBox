package zielu.gittoolbox.util

import com.intellij.openapi.diagnostic.Logger
import java.util.concurrent.ExecutorService

internal object ConcurrentUtil {
  private val log = Logger.getInstance(ConcurrentUtil::class.java)

  fun shutdown(executor: ExecutorService) {
    executor.shutdownNow().forEach { notStarted: Runnable ->
      log.info("Task $notStarted was never started")
    }
  }
}
