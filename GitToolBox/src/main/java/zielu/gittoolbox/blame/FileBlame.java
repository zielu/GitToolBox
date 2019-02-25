package zielu.gittoolbox.blame;

import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.util.text.DateFormatUtil;
import git4idea.i18n.GitBundle;
import java.util.Date;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.ResBundle;

final class FileBlame extends AbstractBlame {
  private final String shortText;
  private final String detailedText;

  private FileBlame(@NotNull VcsRevisionNumber revisionNumber, String author, Date date, String detailedText) {
    super(revisionNumber);
    shortText = prepareAuthor(author) + " "
        + DateFormatUtil.formatBetweenDates(date.getTime(), System.currentTimeMillis());
    this.detailedText = detailedText;
  }

  public static Blame create(@NotNull VcsFileRevision revision) {
    String detailedText = revision.getCommitMessage() + "\n...";
    detailedText = GitBundle.message("annotation.tool.tip", revision.getRevisionNumber().asString(),
        revision.getAuthor(), DateFormatUtil.formatDateTime(revision.getRevisionDate()), detailedText);
    return new FileBlame(revision.getRevisionNumber(), revision.getAuthor(), revision.getRevisionDate(), detailedText);
  }

  @NotNull
  @Override
  public String getShortText() {
    return shortText;
  }

  @Override
  protected String getStatusPrefix() {
    return ResBundle.getString("blame.file.prefix");
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
