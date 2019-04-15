package zielu.junit5.intellij.extension.platform;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NotNull;

public interface PlatformTest {
  void execute(@NotNull Runnable test);

  <T> T executeInEdt(@NotNull Computable<T> test);

  Document getDocument(@NotNull VirtualFile file);

  <L> void subscribe(@NotNull Topic<L> topic, @NotNull L listener);
}
