package zielu.gittoolbox.completion;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import org.jetbrains.annotations.NotNull;

interface CompletionScopeProvider {
  CompletionScopeProvider EMPTY = Collections::emptyList;

  @NotNull
  Collection<File> getAffectedFiles();
}

