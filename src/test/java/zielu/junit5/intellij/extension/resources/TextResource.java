package zielu.junit5.intellij.extension.resources;

import java.util.List;
import org.jetbrains.annotations.NotNull;

public interface TextResource {
  @NotNull
  List<String> getLines();
}
