package zielu.gittoolbox.util;

import com.google.common.collect.Lists;
import com.intellij.openapi.Disposable;
import java.util.Arrays;
import java.util.Collection;
import zielu.gittoolbox.ProjectAware;

public class ProjectAwares implements ProjectAware, Disposable {
  private final Collection<ProjectAware> awares;

  private ProjectAwares(Iterable<? extends ProjectAware> awares) {
    this.awares = Lists.newArrayList(awares);
  }

  public static ProjectAwares create(ProjectAware... awares) {
    return new ProjectAwares(Arrays.asList(awares));
  }

  @Override
  public void opened() {
    for (ProjectAware aware : awares) {
      aware.opened();
    }
  }

  @Override
  public void closed() {
    for (ProjectAware aware : awares) {
      aware.closed();
    }
  }

  @Override
  public void dispose() {
    awares.clear();
  }
}
