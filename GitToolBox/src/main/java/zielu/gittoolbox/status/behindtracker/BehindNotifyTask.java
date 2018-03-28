package zielu.gittoolbox.status.behindtracker;

import com.intellij.openapi.project.Project;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jetbrains.annotations.NotNull;

public class BehindNotifyTask implements Runnable {
  private final Project project;
  private final String projectName;

  public BehindNotifyTask(@NotNull Project project) {
    this.project = project;
    projectName = project.getName();
  }

  @Override
  public void run() {
    BehindTracker.getInstance(project).showChangeNotification();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .append("projectName", projectName)
        .toString();
  }
}
