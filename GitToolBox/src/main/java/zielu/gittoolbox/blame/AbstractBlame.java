package zielu.gittoolbox.blame;

import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.config.AuthorNameType;

abstract class AbstractBlame implements Blame {
  private final VcsRevisionNumber revisionNumber;

  AbstractBlame(@NotNull VcsRevisionNumber revisionNumber) {
    this.revisionNumber = revisionNumber;
  }

  @Nullable
  protected final String prepareAuthor(@Nullable String author) {
    return AuthorNameType.LASTNAME.shorten(author);
  }

  protected abstract String getStatusPrefix();

  @NotNull
  @Override
  public String getShortStatus() {
    return getStatusPrefix() + " " + getShortText();
  }

  @NotNull
  @Override
  public VcsRevisionNumber getRevisionNumber() {
    return revisionNumber;
  }
}
