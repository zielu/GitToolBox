package zielu.gittoolbox.util;

import org.jetbrains.annotations.NotNull;

public interface Cached<T> {
  boolean isLoading();

  boolean isLoaded();

  boolean isEmpty();

  @NotNull
  T value();
}
