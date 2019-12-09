package zielu.gittoolbox.ui.util

import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import zielu.gittoolbox.util.DisposeSafeRunnable

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
    invokeLater(DisposeSafeRunnable(project, task))
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
  fun invokeLaterIfNeeded(project: Project, task: Runnable) {
    invokeLaterIfNeeded(DisposeSafeRunnable(project, task))
  }

  private fun getApplication(): Application {
    return ApplicationManager.getApplication()
  }
}
