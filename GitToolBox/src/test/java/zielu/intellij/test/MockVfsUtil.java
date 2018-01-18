package zielu.intellij.test;

import com.intellij.mock.MockVirtualFile;

public final class MockVfsUtil {
  private MockVfsUtil() {
    throw new IllegalStateException();
  }

  public static MockVirtualFile createDir(String name) {
    return new MockVirtualFile(true, name);
  }

  public static MockVirtualFile createDir(MockVirtualFile parent, String name) {
    MockVirtualFile dir = createDir(name);
    parent.addChild(dir);
    return dir;
  }
}
