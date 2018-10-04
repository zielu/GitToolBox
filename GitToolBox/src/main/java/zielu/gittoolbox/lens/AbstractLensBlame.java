package zielu.gittoolbox.lens;

import com.intellij.openapi.vcs.actions.ShortNameType;
import com.intellij.openapi.vcs.actions.ShowShortenNames;
import org.jetbrains.annotations.Nullable;

abstract class AbstractLensBlame implements LensBlame {
  @Nullable
  protected final String prepareAuthor(@Nullable String author) {
    return ShortNameType.shorten(author, ShowShortenNames.getType());
  }
}
