package zielu.gittoolbox.ui.blame;

import com.intellij.openapi.util.text.StringUtil;
import jodd.util.StringBand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.UtfSeq;
import zielu.gittoolbox.config.AuthorNameType;
import zielu.gittoolbox.config.DateType;
import zielu.gittoolbox.config.GitToolBoxConfig2;
import zielu.gittoolbox.revision.RevisionInfo;
import zielu.gittoolbox.ui.DatePresenter;

import java.util.Date;

class BlamePresenterImpl implements BlamePresenter {
  private static final String SUBJECT_SEPARATOR = " " + UtfSeq.BULLET + " ";
  private static final String COMMIT_PREFIX = ResBundle.message("blame.commit") + " ";
  private static final String AUTHOR_PREFIX = ResBundle.message("blame.author") + " ";
  private static final String DATE_PREFIX = ResBundle.message("blame.date") + " ";

  private final DatePresenter datePresenter;

  BlamePresenterImpl(@NotNull DatePresenter datePresenter) {
    this.datePresenter = datePresenter;
  }

  @NotNull
  @Override
  public String getEditorInline(@NotNull RevisionInfo revisionInfo) {
    StringBand info = new StringBand(5)
        .append(formatInlineAuthor(revisionInfo))
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
        .append(formatStatusAuthor(revisionInfo))
        .append(" ")
        .append(datePresenter.format(DateType.ABSOLUTE, revisionInfo.getDate()))
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
        .append(datePresenter.format(DateType.ABSOLUTE, revisionInfo.getDate()))
        .append("\n");
    if (StringUtil.isNotEmpty(details)) {
      text.append("\n").append(details);
    }
    return text.toString();
  }

  private String formatInlineAuthor(@NotNull RevisionInfo revisionInfo) {
    AuthorNameType nameType = GitToolBoxConfig2.getInstance().blameInlineAuthorNameType;
    switch (nameType) {
      case EMAIL:
      case USERNAME:
        return nameType.shorten(revisionInfo.getEmail());
      default:
        return nameType.shorten(revisionInfo.getAuthor());
    }
  }

  private String formatStatusAuthor(@NotNull RevisionInfo revisionInfo) {
    AuthorNameType nameType = GitToolBoxConfig2.getInstance().blameStatusAuthorNameType;
    switch (nameType) {
      case EMAIL:
      case USERNAME:
        return nameType.shorten(revisionInfo.getEmail());
      default:
        return nameType.shorten(revisionInfo.getAuthor());
    }
  }

  private String formatDate(@Nullable Date date) {
    String formatted = datePresenter.format(GitToolBoxConfig2.getInstance().blameInlineDateType, date);
    if (formatted == null) {
      return "";
    } else {
      return formatted;
    }
  }
}
