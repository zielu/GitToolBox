package zielu.gittoolbox.util;

import org.jetbrains.annotations.NotNull;

public interface Cached<T> {
  boolean isLoading();

  boolean isEmpty();

  @NotNull
  T value();
}
