package zielu.gittoolbox.blame;

import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.intellij.openapi.vcs.annotate.LineAnnotationAspect;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.util.text.DateFormatUtil;
import java.util.Date;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.config.GitToolBoxConfig2;

class BlameFactory {
  Blame forLine(@NotNull FileAnnotation annotation, @NotNull VcsRevisionNumber lineRevision, int lineNumber) {
    String author = null;
    for (LineAnnotationAspect aspect : annotation.getAspects()) {
      if (LineAnnotationAspect.AUTHOR.equals(aspect.getId())) {
        author = aspect.getValue(lineNumber);
      }
    }
    String revisionDate = formatDate(annotation.getLineDate(lineNumber));
    return new LineBlame(lineRevision, formatAuthor(author), revisionDate, annotation.getToolTip(lineNumber));
  }

  private String formatAuthor(@Nullable String author) {
    return GitToolBoxConfig2.getInstance().blameAuthorNameType.shorten(author);
  }

  private String formatDate(@Nullable Date date) {
    if (date != null) {
      return DateFormatUtil.formatBetweenDates(date.getTime(), System.currentTimeMillis());
    } else {
      return "";
    }
  }
}
