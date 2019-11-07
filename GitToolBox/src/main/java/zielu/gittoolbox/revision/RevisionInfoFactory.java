package zielu.gittoolbox.revision;

import org.jetbrains.annotations.NotNull;

interface RevisionInfoFactory {
  @NotNull
  RevisionInfo forLine(@NotNull RevisionDataProvider provider, int lineNumber);
}
