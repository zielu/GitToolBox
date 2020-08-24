package zielu.gittoolbox

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.util.messages.MessageBus
import zielu.gittoolbox.util.AppUtil
import java.util.Optional
import java.util.concurrent.atomic.AtomicBoolean

internal class GitToolBoxApp : Disposable {
    private val active = AtomicBoolean(true)

    override fun dispose() {
        active.compareAndSet(true, false)
    }

    fun runInBackground(task: Runnable) {
        if (active.get()) {
            ApplicationManager.getApplication().executeOnPooledThread(task)
        }
    }

    fun publishSync(project: Project, publisher: (messageBus: MessageBus) -> Unit) {
        if (active.get()) {
            publisher.invoke(project.messageBus)
        }
    }

    companion object {
        fun getInstance(): Optional<GitToolBoxApp> {
            return AppUtil.getServiceInstanceSafe(GitToolBoxApp::class.java)
        }
    }
}
