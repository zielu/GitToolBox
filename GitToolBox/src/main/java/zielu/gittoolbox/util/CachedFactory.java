package zielu.gittoolbox.util;

import org.jetbrains.annotations.NotNull;

public final class CachedFactory {
  private static final Cached LOADING = new Cached<Object>() {
    @Override
    public boolean isLoading() {
      return true;
    }

    @NotNull
    @Override
    public Object value() {
      throw new IllegalStateException("Loading - no value yet. Check with isLoading()");
    }
  };

  private CachedFactory() {
    //do nothing
  }

  public static <T> Cached<T> loading() {
    return LOADING;
  }

  public static <T> Cached<T> ofValue(@NotNull T value) {
    return new CachedImpl<>(value);
  }

  private static class CachedImpl<T> implements Cached<T> {
    private final T value;

    private CachedImpl(T value) {
      this.value = value;
    }

    @Override
    public boolean isLoading() {
      return false;
    }

    @NotNull
    @Override
    public T value() {
      return value;
    }
  }
}
