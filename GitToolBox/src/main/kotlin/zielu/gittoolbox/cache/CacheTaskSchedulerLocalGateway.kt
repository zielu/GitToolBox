package zielu.gittoolbox.cache

internal interface CacheTaskSchedulerLocalGateway {
  fun queueSizeCounterInc()

  fun queueSizeCounterDec()

  fun discardedTasksCounterInc()
}
