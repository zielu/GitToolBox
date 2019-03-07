package zielu.gittoolbox.blame;

import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import java.util.Date;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Blame {
  @NotNull
  VcsRevisionNumber getRevisionNumber();

  @Nullable
  String getAuthor();

  @NotNull
  Date getDate();

  @NotNull
  String getStatusPrefix();

  @Nullable
  String getDetailedText();
}
