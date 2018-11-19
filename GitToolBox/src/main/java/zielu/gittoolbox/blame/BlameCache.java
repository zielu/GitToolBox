package zielu.gittoolbox.blame;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NotNull;

public interface BlameCache {
  Topic<BlameCacheListener> TOPIC = Topic.create("blame cache updates", BlameCacheListener.class);
  @NotNull
  BlameAnnotation getAnnotation(@NotNull VirtualFile file);
  void invalidate(@NotNull VirtualFile file);
}
