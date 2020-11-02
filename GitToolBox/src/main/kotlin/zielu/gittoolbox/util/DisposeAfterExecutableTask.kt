package zielu.gittoolbox.util

import com.intellij.openapi.Disposable
import com.intellij.openapi.util.Disposer

internal class DisposeAfterExecutableTask(
  private val task: ExecutableTask,
  private val disposable: Disposable
) : ExecutableTask {

  override fun run() {
    try {
      task.run()
    } finally {
      Disposer.dispose(disposable)
    }
  }

  override fun getTitle(): String = task.title
}
