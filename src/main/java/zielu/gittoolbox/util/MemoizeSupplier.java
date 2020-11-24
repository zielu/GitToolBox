package zielu.gittoolbox.util;

import java.util.function.Supplier;

public class MemoizeSupplier<T> implements Supplier<T> {
  private final Supplier<T> supplier;
  private volatile T memoized;

  public MemoizeSupplier(Supplier<T> supplier) {
    this.supplier = supplier;
  }

  @Override
  public T get() {
    if (memoized == null) {
      return memorizeAndGet();
    } else {
      return memoized;
    }
  }

  private synchronized T memorizeAndGet() {
    if (memoized == null) {
      memoized = supplier.get();
      return memoized;
    } else {
      return memoized;
    }
  }
}
