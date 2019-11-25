package zielu.gittoolbox.ui.blame;

import com.intellij.openapi.util.text.StringUtil;
import jodd.util.StringBand;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.UtfSeq;
import zielu.gittoolbox.config.AuthorNameType;
import zielu.gittoolbox.config.DateType;
import zielu.gittoolbox.revision.RevisionInfo;
import zielu.gittoolbox.ui.AuthorPresenter;
import zielu.gittoolbox.util.Html;

class BlamePresenterImpl implements BlamePresenter {
  private static final String SUBJECT_SEPARATOR = " " + UtfSeq.BULLET + " ";
  private static final String COMMIT_PREFIX = ResBundle.message("blame.commit") + " ";
  private static final String AUTHOR_PREFIX = ResBundle.message("blame.author") + " ";
  private static final String DATE_PREFIX = ResBundle.message("blame.date") + " ";

  private final BlamePresenterLocalGateway gateway;

  BlamePresenterImpl() {
    this.gateway = new BlamePresenterLocalGateway();
  }

  @NotNull
  @Override
  public String getEditorInline(@NotNull RevisionInfo revisionInfo) {
    String author = formatInlineAuthor(revisionInfo);
    String date = gateway.formatInlineDateTime(revisionInfo.getDate());
    boolean notBlankAuthor = StringUtils.isNotBlank(author);
    boolean notBlankDate = StringUtils.isNotBlank(date);
    StringBand info = new StringBand(5);
    if (notBlankAuthor && notBlankDate) {
      info.append(author).append(", ").append(date);
    } else if (notBlankAuthor) {
      info.append(author);
    } else if (notBlankDate) {
      info.append(date);
    }
    boolean showSubject = gateway.getShowInlineSubject();
    if (showSubject && revisionInfo.getSubject() != null) {
      info.append(SUBJECT_SEPARATOR).append(revisionInfo.getSubject());
    }
    return info.toString();
  }

  @NotNull
  @Override
  public String getStatusBar(@NotNull RevisionInfo revisionInfo) {
    StringBand value = new StringBand(5).append(ResBundle.message("blame.prefix"));
    String author = formatStatusAuthor(revisionInfo);
    if (StringUtils.isNotBlank(author)) {
      value.append(" ").append(author);
    }
    value.append(" ").append(gateway.formatDateTime(DateType.ABSOLUTE, revisionInfo.getDate()));
    return value.toString();
  }

  @NotNull
  @Override
  public String getPopup(@NotNull RevisionInfo revisionInfo, @Nullable String details) {
    StringBand text = new StringBand(11)
        .append(COMMIT_PREFIX)
        .append(revisionInfo.getRevisionNumber().asString());

    String author = formatPopupAuthor(revisionInfo);
    if (StringUtils.isNotBlank(author)) {
      text.append(Html.BR).append(AUTHOR_PREFIX).append(author);
    }

    text.append(Html.BR)
        .append(DATE_PREFIX)
        .append(gateway.formatDateTime(DateType.ABSOLUTE, revisionInfo.getDate()))
        .append(Html.BR);
    if (StringUtil.isNotEmpty(details)) {
      text.append(Html.BR).append(details);
    }
    return text.toString();
  }

  private String formatInlineAuthor(@NotNull RevisionInfo revisionInfo) {
    return formatAuthor(gateway.getInlineAuthorNameType(), revisionInfo);
  }

  private String formatStatusAuthor(@NotNull RevisionInfo revisionInfo) {
    return formatAuthor(gateway.getStatusAuthorNameTYpe(), revisionInfo);
  }

  private String formatPopupAuthor(@NotNull RevisionInfo revisionInfo) {
    StringBand formatted = new StringBand(5);
    formatted.append(formatAuthor(AuthorNameType.FULL, revisionInfo));
    if (revisionInfo.getAuthorEmail() != null) {
      formatted.append(" ");
      formatted.append(Html.LT);
      formatted.append(revisionInfo.getAuthorEmail());
      formatted.append(Html.GT);
    }
    return formatted.toString();
  }

  private String formatAuthor(@NotNull AuthorNameType type, @NotNull RevisionInfo revisionInfo) {
    if (type == AuthorNameType.EMAIL || type == AuthorNameType.EMAIL_USER) {
      if (revisionInfo.getAuthorEmail() == null) {
        return AuthorPresenter.format(type, revisionInfo.getAuthor(), revisionInfo.getAuthor());
      }
    }
    return AuthorPresenter.format(type, revisionInfo.getAuthor(), revisionInfo.getAuthorEmail());
  }
}
