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

  public static <T> Cached<T> loading(@NotNull T value) {
    return new CachedImpl<>(value, true);
  }

  public static <T> Cached<T> loaded(@NotNull T value) {
    return new CachedImpl<>(value, false);
  }

  private static class CachedImpl<T> implements Cached<T> {
    private final T value;
    private final boolean loading;

    private CachedImpl(T value, boolean loading) {
      this.value = value;
      this.loading = loading;
    }

    @Override
    public boolean isLoading() {
      return loading;
    }

    @NotNull
    @Override
    public T value() {
      return value;
    }
  }
}
