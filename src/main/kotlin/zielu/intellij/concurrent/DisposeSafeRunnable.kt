package zielu.intellij.concurrent

import com.intellij.openapi.diagnostic.Logger
import org.apache.commons.lang3.builder.ToStringBuilder
import org.apache.commons.lang3.builder.ToStringStyle
import zielu.intellij.util.ZDisposedException

internal class DisposeSafeRunnable(
  private val operation: Runnable
) : Runnable {

  constructor(task: () -> Unit) : this(Runnable { task.invoke() })

  override fun run() {
    try {
      operation.run()
    } catch (error: ZDisposedException) {
      handleError(error)
    } catch (error: InterruptedException) {
      handleError(error)
    }
  }

  private fun handleError(error: Throwable) {
    log.info("Already disposed", error)
  }

  override fun toString(): String {
    return ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
      .append("operation", operation)
      .toString()
  }

  private companion object {
    private val log = Logger.getInstance(DisposeSafeRunnable::class.java)
  }
}
