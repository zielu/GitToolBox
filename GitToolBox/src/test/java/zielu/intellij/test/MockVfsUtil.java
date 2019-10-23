package zielu.intellij.test;

import com.intellij.mock.MockVirtualFile;
import org.jetbrains.annotations.NotNull;

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

  public static MockVirtualFile createFile(@NotNull String name) {
    return new MockVirtualFile(false, name);
  }

  public static MockVirtualFile createFile(MockVirtualFile parent, String name) {
    MockVirtualFile file = createFile(name);
    parent.addChild(file);
    return file;
  }
}
