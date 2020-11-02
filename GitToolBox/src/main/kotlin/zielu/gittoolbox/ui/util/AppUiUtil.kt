package zielu.gittoolbox.ui.util

import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import zielu.intellij.concurrent.DisposeSafeRunnable

internal object AppUiUtil {
  @JvmStatic
  fun invokeLater(task: Runnable) {
    val application = getApplication()
    if (application.isUnitTestMode) {
      task.run()
    } else {
      application.invokeLater(task)
    }
  }

  @JvmStatic
  fun invokeLater(project: Project, task: Runnable) {
    // TODO: replace project with Disposable
    invokeLater(task)
  }

  @JvmStatic
  fun invokeLaterIfNeeded(task: Runnable) {
    val application = getApplication()
    if (application.isUnitTestMode || application.isDispatchThread) {
      task.run()
    } else {
      application.invokeLater(task)
    }
  }

  @JvmStatic
  fun invokeAndWaitIfNeeded(task: Runnable) {
    val application = getApplication()
    if (application.isUnitTestMode || application.isDispatchThread) {
      task.run()
    } else {
      application.invokeAndWait(task)
    }
  }

  @JvmStatic
  fun invokeLaterIfNeeded(project: Project, task: Runnable) {
    // TODO: replace project with Disposable
    invokeLaterIfNeeded(DisposeSafeRunnable(task))
  }

  private fun getApplication(): Application {
    return ApplicationManager.getApplication()
  }
}
