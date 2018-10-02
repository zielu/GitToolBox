package zielu.gittoolbox.lens;

import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.intellij.openapi.vcs.annotate.LineAnnotationAspect;
import org.jetbrains.annotations.NotNull;

public class LensLineBlame implements LensBlame {
  private final String author;
  private final String revisionDate;

  private LensLineBlame(String author, String revisionDate) {
    this.author = author;
    this.revisionDate = revisionDate;
  }

  public static LensBlame create(@NotNull FileAnnotation annotation, int line) {
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
    return new LensLineBlame(author, revisionDate);
  }

  @NotNull
  @Override
  public String getPresentableText() {
    return author + " " + revisionDate;
  }
}
