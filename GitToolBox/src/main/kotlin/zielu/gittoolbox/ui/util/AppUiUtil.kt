package zielu.gittoolbox.ui.util

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import zielu.gittoolbox.GitToolBoxPrj
import zielu.intellij.concurrent.ZDisposableRunnable

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
  fun invokeLater(disposable: Disposable, task: Runnable) {
    val toDo = ZDisposableRunnable(task)
    Disposer.register(disposable, toDo)
    invokeLater(toDo)
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
    invokeLaterIfNeeded(GitToolBoxPrj.getInstance(project), task)
  }

  @JvmStatic
  fun invokeLaterIfNeeded(disposable: Disposable, task: Runnable) {
    val toDo = ZDisposableRunnable(task)
    Disposer.register(disposable, toDo)
    invokeLaterIfNeeded(toDo)
  }

  private fun getApplication(): Application {
    return ApplicationManager.getApplication()
  }
}
