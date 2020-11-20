package zielu.junit5.intellij.util;

import static com.google.common.base.Preconditions.checkArgument;

import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.jetbrains.annotations.NotNull;

public class TestUtil {
  private TestUtil() {
    // do nothing
  }

  @NotNull
  public static Path resolvePathFromParts(@NotNull String[] parts) {
    checkArgument(parts.length > 0, "Path has zero parts");
    Path path = null;
    if (parts.length == 1) {
      path = Paths.get(parts[0]);
    } else {
      path = Paths.get(parts[0], Arrays.copyOfRange(parts, 1, parts.length));
    }
    return path.normalize().toAbsolutePath();
  }

  public static VirtualFile findVfForPath(Path path) {
    return VfsUtil.findFile(path, true);
  }
}
