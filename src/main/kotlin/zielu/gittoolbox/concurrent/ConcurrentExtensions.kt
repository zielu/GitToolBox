package zielu.gittoolbox.concurrent

import zielu.gittoolbox.GitToolBoxApp
import zielu.intellij.concurrent.ZCompletableBackgroundable
import java.util.concurrent.CompletableFuture

internal fun <T> ZCompletableBackgroundable<T>.executeAsync(): CompletableFuture<T> {
  return this.asCompletableFuture(GitToolBoxApp.getInstance().orElseThrow().asyncExecutor())
}
