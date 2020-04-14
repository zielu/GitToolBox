package zielu.intellij.java

import java.util.Objects
import java.util.stream.Collectors
import java.util.stream.Stream
import kotlin.streams.toList

internal fun <T, R> Stream<T>.mapNotNull(mapper: (T) -> R?): Stream<R> {
  return this.filter(Objects::nonNull).map(mapper)
}

internal fun <T> Stream<T>.toSet(): Set<T> = collect(Collectors.toSet<T>())

internal fun <T> Stream<T>.firstOrNull(predicate: (T) -> Boolean): T? {
  return this.filter(predicate).findFirst().orElse(null)
}

internal fun <T> Stream<T>.singleOrNull(): T? {
  val items = this.limit(2).toList()
  return if (items.size == 1) {
    items[0]
  } else {
    null
  }
}
