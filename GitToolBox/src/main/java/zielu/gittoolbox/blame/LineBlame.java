package zielu.gittoolbox.blame;

import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.intellij.openapi.vcs.annotate.LineAnnotationAspect;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.ResBundle;

public class LineBlame extends AbstractBlame {
  private final String shortText;
  private final String detailedText;

  private LineBlame(@NotNull VcsRevisionNumber revisionNumber, String author, String revisionDate,
                    String detailedText) {
    super(revisionNumber);
    shortText = prepareAuthor(author) + " " + revisionDate;
    this.detailedText = detailedText;
  }

  public static Blame create(@NotNull FileAnnotation annotation, @NotNull VcsRevisionNumber revisionNumber,
                             int line) {
    String author = null;
    String revisionDate = null;
    for (LineAnnotationAspect aspect : annotation.getAspects()) {
      if (LineAnnotationAspect.AUTHOR.equals(aspect.getId())) {
        author = aspect.getValue(line);
      } else if (LineAnnotationAspect.DATE.equals(aspect.getId())) {
        revisionDate = aspect.getValue(line);
      }
    }
    return new LineBlame(revisionNumber, author, revisionDate, annotation.getToolTip(line));
  }

  @NotNull
  @Override
  public String getShortText() {
    return shortText;
  }

  @Override
  protected String getStatusPrefix() {
    return ResBundle.getString("blame.line.prefix");
  }

  @Nullable
  @Override
  public String getDetailedText() {
    return detailedText;
  }
}
