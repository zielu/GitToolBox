package zielu.gittoolbox.util

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.apache.commons.lang3.builder.ToStringBuilder
import org.apache.commons.lang3.builder.ToStringStyle
import java.util.concurrent.Callable

internal class DisposeSafeCallable<T> (
  private val project: Project,
  private val operation: Callable<T>,
  private val disposedResult: T
) : Callable<T> {

  @Throws(Exception::class)
  override fun call(): T {
    return try {
      if (project.isDisposed) {
        disposedResult
      } else {
        operation.call()
      }
    } catch (error: AssertionError) {
      if (project.isDisposed) {
        log.debug("Project already disposed", error)
        disposedResult
      } else {
        log.error(error)
        disposedResult
      }
    }
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
