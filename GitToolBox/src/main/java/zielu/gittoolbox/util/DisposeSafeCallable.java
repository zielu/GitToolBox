package zielu.gittoolbox.util;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import java.util.concurrent.Callable;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;

public class DisposeSafeCallable<T> implements Callable<T> {
  private final Logger log = Logger.getInstance(getClass());
  private final Project project;
  @NotNull
  private final Callable<T> operation;
  private final T disposedResult;

  public DisposeSafeCallable(@NotNull Project project, @NotNull Callable<T> operation, T disposedResult) {
    this.project = project;
    this.operation = operation;
    this.disposedResult = disposedResult;
  }

  @Override
  public T call() throws Exception {
    try {
      return operation.call();
    } catch (AssertionError error) {
      if (project.isDisposed()) {
        log.debug("Project already disposed", error);
        return disposedResult;
      } else {
        log.error(error);
        return disposedResult;
      }
    }
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("operation", operation)
        .append("disposedResult", disposedResult)
        .toString();
  }
}
