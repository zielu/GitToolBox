package zielu.gittoolbox.util

import com.intellij.openapi.diagnostic.Logger
import zielu.intellij.util.ZDisposedException

internal class DisposeSafeExecutableTask(private val task: ExecutableTask) : ExecutableTask {
  override fun run() {
    try {
      task.run()
    } catch (error: ZDisposedException) {
      handleError(error)
    } catch (error: InterruptedException) {
      handleError(error)
    }
  }

  override fun getTitle(): String = task.title

  private fun handleError(error: Throwable) {
    log.info("Already disposed", error)
  }

  private companion object {
    private val log = Logger.getInstance(DisposeSafeExecutableTask::class.java)
  }
}
