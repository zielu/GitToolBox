package zielu.gittoolbox.blame;

import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.intellij.openapi.vcs.annotate.LineAnnotationAspect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.ResBundle;

public class LineBlame extends AbstractBlame {
  private final String shortText;
  private final String detailedText;

  private LineBlame(String author, String revisionDate, String detailedText) {
    shortText = prepareAuthor(author) + " " + revisionDate;
    this.detailedText = detailedText;
  }

  public static Blame create(@NotNull FileAnnotation annotation, int line) {
    LineAnnotationAspect[] aspects = annotation.getAspects();
    String author = null;
    String revisionDate = null;
    for (LineAnnotationAspect aspect : aspects) {
      if (LineAnnotationAspect.AUTHOR.equals(aspect.getId())) {
        author = aspect.getValue(line);
      } else if (LineAnnotationAspect.DATE.equals(aspect.getId())) {
        revisionDate = aspect.getValue(line);
      }
    }
    return new LineBlame(author, revisionDate, annotation.getToolTip(line));
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
