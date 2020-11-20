package zielu.junit5.intellij.extension.mocks;

public interface IdeaMocks {
  <T> T mockListener(Class<T> listener);
}
