package zielu.gittoolbox.ui.blame;

import java.time.LocalDate;
import jodd.util.StringBand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.blame.Blame;
import zielu.gittoolbox.config.GitToolBoxConfig2;

class BlamePresenterImpl implements BlamePresenter {
  @NotNull
  @Override
  public String getEditorInline(@NotNull Blame blame) {
    return new StringBand(3)
      .append(formatAuthor(blame.getAuthor()))
      .append(" ")
      .append(formatDate(blame.getDate()))
      .toString();
  }

  @NotNull
  @Override
  public String getStatusBar(@NotNull Blame blame) {
    return new StringBand(5)
        .append(ResBundle.getString("blame.prefix"))
        .append(" ")
        .append(formatAuthor(blame.getAuthor()))
        .append(" ")
        .append(formatDate(blame.getDate()))
        .toString();
  }

  @NotNull
  @Override
  public String getPopup(@NotNull Blame blame) {
    String details = blame.getDetails();
    if (details != null) {
      return new StringBand(6)
          .append("commit: ")
          .append(blame.getRevisionNumber().asString())
          .append("\nauthor: ")
          .append(formatAuthor(blame.getAuthor()))
          .append("\n\n")
          .append(details)
          .toString();
    } else {
      return ResBundle.naLabel();
    }
  }

  private String formatAuthor(@Nullable String author) {
    return GitToolBoxConfig2.getInstance().blameAuthorNameType.shorten(author);
  }

  private String formatDate(@Nullable LocalDate date) {
    if (date != null) {
      return GitToolBoxConfig2.getInstance().blameDateType.format(date);
    } else {
      return "";
    }
  }
}
