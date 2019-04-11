package zielu.junit5.intellij;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public interface PlatformTest {
  void execute(Runnable test);
  <T> T executeInEdt(Computable<T> test);
  Document getDocument(@NotNull VirtualFile file);
}
