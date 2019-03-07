package zielu.gittoolbox.blame;

import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.util.text.DateFormatUtil;
import java.util.Date;
import jodd.util.StringBand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.ResBundle;

public final class FileBlame extends AbstractBlame {
  private static final String COMMIT_PREFIX = ResBundle.getString("blame.commit") + " ";
  private static final String AUTHOR_PREFIX = ResBundle.getString("blame.author") + " ";
  private static final String DATE_PREFIX = ResBundle.getString("blame.date") + " ";

  private final String detailedText;

  private FileBlame(@NotNull VcsRevisionNumber revisionNumber, String author, @NotNull Date date, String detailedText) {
    super(revisionNumber, author, date);
    this.detailedText = detailedText;
  }

  public static Blame create(@NotNull VcsFileRevision revision) {
    StringBand detailedText = new StringBand(11)
        .append(COMMIT_PREFIX)
        .append(revision.getRevisionNumber().asString())
        .append("\n")
        .append(AUTHOR_PREFIX)
        .append(revision.getAuthor())
        .append("\n")
        .append(DATE_PREFIX)
        .append(DateFormatUtil.formatDateTime(revision.getRevisionDate()))
        .append("\n\n")
        .append(revision.getCommitMessage())
        .append("\n...");
    return new FileBlame(revision.getRevisionNumber(), revision.getAuthor(), revision.getRevisionDate(),
        detailedText.toString());
  }

  @NotNull
  @Override
  public String getStatusPrefix() {
    return ResBundle.getString("blame.file.prefix");
  }

  @Nullable
  @Override
  public String getDetailedText() {
    return detailedText;
  }
}
