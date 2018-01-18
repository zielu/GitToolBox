package zielu.junit5.intellij;

import static org.mockito.Mockito.mock;

import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.Map;

class IdeaMocksImpl implements IdeaMocks {
  private final Map<Class<?>, Object> mockListeners = new HashMap<>();

  @Override
  public <T> T mockListener(Class<T> listenerClass) {
    T mockListener = mock(listenerClass);
    mockListeners.put(listenerClass, mockListener);
    return mockListener;
  }

  <T> boolean hasMockListener(Class<T> listenerClass) {
    return mockListeners.containsKey(listenerClass);
  }

  <T> T getMockListener(Class<T> listenerClass) {
    return listenerClass.cast(Preconditions.checkNotNull(mockListeners.get(listenerClass)));
  }
}
