package zielu.gittoolbox.revision;

import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

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
    public String getEmail() {
      return EMPTY_TEXT;
    }

    @NotNull
    @Override
    public Date getDate() {
      return new Date();
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
  String getEmail();

  @NotNull
  Date getDate();

  @Nullable
  String getSubject();

  boolean isEmpty();

  boolean isNotEmpty();
}
