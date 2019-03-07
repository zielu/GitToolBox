package zielu.gittoolbox.blame;

import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.intellij.openapi.vcs.annotate.LineAnnotationAspect;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import java.util.Date;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.ResBundle;

public class LineBlame extends AbstractBlame {
  private final String detailedText;

  private LineBlame(@NotNull VcsRevisionNumber revisionNumber, String author, @NotNull Date date,
                    String detailedText) {
    super(revisionNumber, author, date);
    this.detailedText = detailedText;
  }

  public static Blame create(@NotNull FileAnnotation annotation, @NotNull VcsRevisionNumber revisionNumber,
                             int line) {
    String author = null;
    for (LineAnnotationAspect aspect : annotation.getAspects()) {
      if (LineAnnotationAspect.AUTHOR.equals(aspect.getId())) {
        author = aspect.getValue(line);
      }
    }
    Date date = annotation.getLineDate(line);
    if (date == null) {
      date = new Date();
    }
    return new LineBlame(revisionNumber, author, date, annotation.getToolTip(line));
  }

  @NotNull
  @Override
  public String getStatusPrefix() {
    return ResBundle.getString("blame.line.prefix");
  }

  @Nullable
  @Override
  public String getDetailedText() {
    return detailedText;
  }
}
