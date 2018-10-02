package zielu.gittoolbox.lens;

import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.util.text.DateFormatUtil;
import java.util.Date;
import org.jetbrains.annotations.NotNull;

public final class LensFileBlame implements LensBlame {
  private final String author;
  private final Date date;

  private LensFileBlame(String author, Date date) {
    this.author = author;
    this.date = date;
  }

  public static LensBlame create(@NotNull VcsFileRevision revision) {
    return new LensFileBlame(revision.getAuthor(), revision.getRevisionDate());
  }

  public String getPresentableText() {
    return author + " " + DateFormatUtil.formatBetweenDates(date.getTime(), System.currentTimeMillis());
  }
}
