package zielu.gittoolbox.blame;

import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.ResBundle;

final class LineBlame extends AbstractBlame {
  private final String shortText;
  private final String detailedText;

  LineBlame(@NotNull VcsRevisionNumber revisionNumber, String author, String revisionDate,
                    String detailedText) {
    super(revisionNumber);
    shortText = author + " " + revisionDate;
    this.detailedText = detailedText;
  }

  @NotNull
  @Override
  public String getShortText() {
    return shortText;
  }

  @Override
  protected String getStatusPrefix() {
    return ResBundle.getString("blame.line.prefix");
  }

  @Nullable
  @Override
  public String getDetailedText() {
    return detailedText;
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public boolean isNotEmpty() {
    return true;
  }
}
