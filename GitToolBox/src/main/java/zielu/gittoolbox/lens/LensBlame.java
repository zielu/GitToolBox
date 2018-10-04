package zielu.gittoolbox.lens;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface LensBlame {
  @NotNull
  String getShortText();

  @Nullable
  String getDetailedText();
}
