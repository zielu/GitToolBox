package zielu.gittoolbox.ui.blame;

import java.util.Date;
import jodd.util.StringBand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.UtfSeq;
import zielu.gittoolbox.config.AuthorNameType;
import zielu.gittoolbox.config.DateType;
import zielu.gittoolbox.config.GitToolBoxConfig2;
import zielu.gittoolbox.revision.RevisionInfo;

class BlamePresenterImpl implements BlamePresenter {
  private static final String SUBJECT_SEPARATOR = " " + UtfSeq.BULLET + " ";
  private static final String COMMIT_PREFIX = ResBundle.message("blame.commit") + " ";
  private static final String AUTHOR_PREFIX = ResBundle.message("blame.author") + " ";
  private static final String DATE_PREFIX = ResBundle.message("blame.date") + " ";

  @NotNull
  @Override
  public String getEditorInline(@NotNull RevisionInfo revisionInfo) {
    StringBand info = new StringBand(5)
        .append(formatInlineAuthor(revisionInfo.getAuthor()))
        .append(", ")
        .append(formatDate(revisionInfo.getDate()));
    boolean showSubject = GitToolBoxConfig2.getInstance().blameInlineShowSubject;
    if (showSubject && revisionInfo.getSubject() != null) {
      info.append(SUBJECT_SEPARATOR).append(revisionInfo.getSubject());
    }
    return info.toString();
  }

  @NotNull
  @Override
  public String getStatusBar(@NotNull RevisionInfo revisionInfo) {
    return new StringBand(5)
        .append(ResBundle.message("blame.prefix"))
        .append(" ")
        .append(formatStatusAuthor(revisionInfo.getAuthor()))
        .append(" ")
        .append(DateType.ABSOLUTE.format(revisionInfo.getDate()))
        .toString();
  }

  @NotNull
  @Override
  public String getPopup(@NotNull RevisionInfo revisionInfo, @Nullable String details) {
    StringBand text = new StringBand(11)
        .append(COMMIT_PREFIX)
        .append(revisionInfo.getRevisionNumber().asString())
        .append("\n")
        .append(AUTHOR_PREFIX)
        .append(AuthorNameType.FULL.shorten(revisionInfo.getAuthor()))
        .append("\n")
        .append(DATE_PREFIX)
        .append(DateType.ABSOLUTE.format(revisionInfo.getDate()))
        .append("\n");
    if (details != null) {
      text.append("\n").append(details);
    }
    return text.toString();
  }

  private String formatInlineAuthor(@Nullable String author) {
    return GitToolBoxConfig2.getInstance().blameInlineAuthorNameType.shorten(author);
  }

  private String formatStatusAuthor(@Nullable String author) {
    return GitToolBoxConfig2.getInstance().blameStatusAuthorNameType.shorten(author);
  }

  private String formatDate(@Nullable Date date) {
    if (date != null) {
      return GitToolBoxConfig2.getInstance().blameInlineDateType.format(date);
    } else {
      return "";
    }
  }
}
