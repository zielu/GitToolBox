package zielu.gittoolbox.revision;

import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import java.time.ZonedDateTime;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface RevisionInfo {
  RevisionInfo EMPTY = new RevisionInfo() {
    private static final String EMPTY_TEXT = "EMPTY";

    @NotNull
    @Override
    public VcsRevisionNumber getRevisionNumber() {
      return VcsRevisionNumber.NULL;
    }

    @NotNull
    @Override
    public String getAuthor() {
      return EMPTY_TEXT;
    }

    @NotNull
    @Override
    public ZonedDateTime getDate() {
      return ZonedDateTime.now();
    }

    @Nullable
    @Override
    public String getSubject() {
      return null;
    }

    @Override
    public boolean isEmpty() {
      return true;
    }

    @Override
    public boolean isNotEmpty() {
      return false;
    }
  };

  @NotNull
  VcsRevisionNumber getRevisionNumber();

  @NotNull
  String getAuthor();

  @NotNull
  ZonedDateTime getDate();

  @Nullable
  String getSubject();

  boolean isEmpty();

  boolean isNotEmpty();
}
