package zielu.intellij.test;

import java.util.function.Supplier;
import org.mockito.stubbing.Answer;

public class MockUtil {
  private MockUtil() {
    throw new IllegalStateException();
  }

  @SuppressWarnings("unchecked")
  public static  <T> Answer<T> callSupplier() {
    return invocation -> ((Supplier<T>) invocation.getArgument(0)).get();
  }
}
