package zielu.gittoolbox.blame;

import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Blame {
  @NotNull
  VcsRevisionNumber getRevisionNumber();

  @NotNull
  String getShortText();

  @NotNull
  String getShortStatus();

  @Nullable
  String getDetailedText();
}
