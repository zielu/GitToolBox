package zielu.gittoolbox.ui.util

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.application.Application
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.openapi.wm.WindowManager
import zielu.gittoolbox.GitToolBoxPrj
import zielu.intellij.concurrent.ZDisposableRunnableWrapper
import java.awt.Component

internal object AppUiUtil {
  private val log = Logger.getInstance(AppUiUtil::class.java)

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
    invokeLater(GitToolBoxPrj.getInstance(project), task)
  }

  @JvmStatic
  fun invokeLater(disposable: Disposable, task: Runnable) {
    val toDo = ZDisposableRunnableWrapper(task)
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
    val toDo = ZDisposableRunnableWrapper(task)
    Disposer.register(disposable, toDo)
    invokeLaterIfNeeded(toDo)
  }

  fun prepareDataContextWithContextComponent(project: Project): DataContext {
    var component: Component? = IdeFocusManager.getInstance(project).focusOwner

    if (component == null) {
      log.info("Missing project focus owner, find global one")
      component = IdeFocusManager.getGlobalInstance().focusOwner
    }
    if (component == null) {
      log.warn("Missing global focus owner, find project frame")
      component = WindowManager.getInstance().getFrame(project)
    }
    if (component == null) {
      log.warn("Missing project frame, find visible frame")
      component = WindowManager.getInstance().findVisibleFrame()
    }
    if (component == null) {
      log.warn("Missing context component")
    }
    return contextWithComponent(SimpleDataContext.getProjectContext(project), component)
  }

  private fun contextWithComponent(parent: DataContext, component: Component?): DataContext {
    if (component == null) {
      return parent
    }

    return SimpleDataContext.getSimpleContext(
      mapOf(
        PlatformDataKeys.CONTEXT_COMPONENT.name to component
      ),
      parent
    )
  }

  private fun getApplication(): Application {
    return ApplicationManager.getApplication()
  }
}
