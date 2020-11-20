package zielu.gittoolbox.cache

internal interface CacheTaskSchedulerLocalGateway {
  fun queueSizeCounterInc()

  fun queueSizeCounterDec()

  fun discardedTasksCounterInc()

  fun schedule(task: Runnable, taskDelayMillis: Long)
}
