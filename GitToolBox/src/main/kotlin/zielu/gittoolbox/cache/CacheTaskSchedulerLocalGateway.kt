package zielu.gittoolbox.cache

import com.intellij.openapi.Disposable

interface CacheTaskSchedulerLocalGateway {
  fun queueSizeCounterInc()

  fun queueSizeCounterDec()

  fun discardedTasksCounterInc()

  fun disposeWithProject(disposable: Disposable)
}
