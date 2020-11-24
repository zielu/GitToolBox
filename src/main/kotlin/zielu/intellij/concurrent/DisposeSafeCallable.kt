package zielu.intellij.concurrent

import com.intellij.openapi.diagnostic.Logger
import org.apache.commons.lang3.builder.ToStringBuilder
import org.apache.commons.lang3.builder.ToStringStyle
import zielu.intellij.util.ZDisposedException
import java.util.concurrent.Callable

internal class DisposeSafeCallable<T>
constructor(
  private val operation: Callable<T>,
  private val disposedResult: T
) : Callable<T> {
  @Throws(Exception::class)
  override fun call(): T {
    return try {
      operation.call()
    } catch (error: ZDisposedException) {
      handleError(error)
    } catch (error: InterruptedException) {
      handleError(error)
    }
  }

  private fun handleError(error: Throwable): T {
    log.info("Already disposed", error)
    return disposedResult
  }

  override fun toString(): String {
    return ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
      .append("operation", operation)
      .append("disposedResult", disposedResult)
      .toString()
  }

  private companion object {
    private val log = Logger.getInstance(DisposeSafeCallable::class.java)
  }
}
