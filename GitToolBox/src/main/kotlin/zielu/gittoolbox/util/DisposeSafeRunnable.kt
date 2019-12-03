package zielu.gittoolbox.util

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.apache.commons.lang3.builder.ToStringBuilder
import org.apache.commons.lang3.builder.ToStringStyle

internal class DisposeSafeRunnable(
  private val project: Project,
  private val operation: Runnable
) : Runnable {

  constructor(project: Project, task: () -> Unit) : this(project, Runnable { task.invoke() })

  override fun run() {
    try {
      if (!project.isDisposed) {
        operation.run()
      }
    } catch (error: AssertionError) {
      if (project.isDisposed) {
        log.debug("Project already disposed", error)
      } else {
        log.error(error)
      }
    }
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
