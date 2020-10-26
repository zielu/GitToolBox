package zielu.intellij.concurrent

import java.util.concurrent.ThreadFactory

internal class ThreadGroupThreadFactory(
  private val group: ThreadGroup
) : ThreadFactory {
  override fun newThread(task: Runnable): Thread {
    return Thread(group, task)
  }
}
