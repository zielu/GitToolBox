package zielu.gittoolbox.util

import com.intellij.openapi.Disposable
import com.intellij.openapi.util.Disposer
import com.intellij.util.messages.MessageBus
import zielu.gittoolbox.GitToolBoxApp
import zielu.intellij.concurrent.DisposeSafeRunnable
import zielu.intellij.concurrent.ZDisposableRunnableWrapper

internal abstract class BaseFacade {
  fun registerDisposable(parent: Disposable, child: Disposable) {
    Disposer.register(parent, child)
  }

  fun dispose(subject: Disposable) {
    Disposer.dispose(subject)
  }

  protected fun publishAppSync(publisher: (messageBus: MessageBus) -> Unit) {
    GitToolBoxApp.getInstance().ifPresent { it.publishSync(publisher) }
  }

  protected fun publishAppAsync(disposable: Disposable, publisher: (messageBus: MessageBus) -> Unit) {
    val task = ZDisposableRunnableWrapper { publishAppSync(publisher) }
    registerDisposable(disposable, task)
    GitToolBoxApp.getInstance().ifPresent { it.runInBackground(DisposeSafeRunnable(task)) }
  }
}
