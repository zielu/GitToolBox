package zielu.gittoolbox.ui.blame;

import java.util.Date;
import jodd.util.StringBand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.blame.Blame;
import zielu.gittoolbox.config.AuthorNameType;
import zielu.gittoolbox.config.DateType;
import zielu.gittoolbox.config.GitToolBoxConfig2;

class BlamePresenterImpl implements BlamePresenter {

  @NotNull
  @Override
  public String getEditorInline(@NotNull Blame blame) {
    StringBand info = new StringBand(3)
        .append(formatAuthor(blame.getAuthor()))
        .append(", ")
        .append(formatDate(blame.getDate()));
    return info.toString();
  }

  @NotNull
  @Override
  public String getStatusBar(@NotNull Blame blame) {
    return new StringBand(5)
        .append(blame.getStatusPrefix())
        .append(" ")
        .append(AuthorNameType.LASTNAME.shorten(blame.getAuthor()))
        .append(" ")
        .append(DateType.ABSOLUTE.format(blame.getDate()))
        .toString();
  }

  @NotNull
  @Override
  public String getPopup(@NotNull Blame blame) {
    String details = blame.getDetailedText();
    return details != null ? details : ResBundle.naLabel();
  }

  private String formatAuthor(@Nullable String author) {
    return GitToolBoxConfig2.getInstance().blameInlineAuthorNameType.shorten(author);
  }

  private String formatDate(@Nullable Date date) {
    if (date != null) {
      return GitToolBoxConfig2.getInstance().blameInlineDateType.format(date);
    } else {
      return "";
    }
  }
}
