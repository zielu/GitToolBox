package zielu.gittoolbox.blame;

import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import java.time.LocalDate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Blame {
  Blame EMPTY = new Blame() {
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
    public LocalDate getDate() {
      return LocalDate.now();
    }

    @Override
    public String getDetails() {
      return EMPTY_TEXT;
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
  LocalDate getDate();

  @Nullable
  String getDetails();

  boolean isEmpty();

  boolean isNotEmpty();
}
