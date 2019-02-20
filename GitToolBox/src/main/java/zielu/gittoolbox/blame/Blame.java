package zielu.gittoolbox.blame;

import com.intellij.openapi.vcs.history.VcsRevisionNumber;
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
    public String getShortText() {
      return EMPTY_TEXT;
    }

    @NotNull
    @Override
    public String getShortStatus() {
      return EMPTY_TEXT;
    }

    @Nullable
    @Override
    public String getDetailedText() {
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
  String getShortText();

  @NotNull
  String getShortStatus();

  @Nullable
  String getDetailedText();

  boolean isEmpty();

  boolean isNotEmpty();
}
