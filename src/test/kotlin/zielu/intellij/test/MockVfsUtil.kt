package zielu.intellij.test

import com.intellij.mock.MockVirtualFile

internal object MockVfsUtil {
  @JvmStatic
  fun createDir(name: String): MockVirtualFile {
    return MockVirtualFile(true, name)
  }

  @JvmStatic
  fun createDir(parent: MockVirtualFile, name: String): MockVirtualFile {
    val dir = createDir(name)
    parent.addChild(dir)
    return dir
  }

  @JvmStatic
  fun createFile(name: String): MockVirtualFile {
    return MockVirtualFile(name)
  }

  @JvmStatic
  fun createFile(parent: MockVirtualFile, name: String): MockVirtualFile {
    val file = createFile(name)
    parent.addChild(file)
    return file
  }
}
