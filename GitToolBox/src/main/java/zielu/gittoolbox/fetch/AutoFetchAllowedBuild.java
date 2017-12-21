package zielu.gittoolbox.fetch;

import com.intellij.compiler.server.BuildManagerListener;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import zielu.gittoolbox.extension.AutoFetchAllowed;

public class AutoFetchAllowedBuild implements AutoFetchAllowed {
  private final Logger log = Logger.getInstance(getClass());

  private final AtomicBoolean buildRunning = new AtomicBoolean();
  private MessageBusConnection connection;

  @Override
  public boolean isAllowed() {
    return !buildRunning.get();
  }

  @Override
  public void initialize(Project project) {
    connection = project.getMessageBus().connect();
    connection.subscribe(BuildManagerListener.TOPIC, new BuildManagerListener() {
      @Override
      public void beforeBuildProcessStarted(Project project, UUID sessionId) {
        //do nothing
      }

      @Override
      public void buildStarted(Project currentProject, UUID sessionId, boolean isAutomake) {
        log.debug("Build started");
        if (Objects.equals(project, currentProject)) {
          buildRunning.set(true);
        }
      }

      @Override
      public void buildFinished(Project currentProject, UUID sessionId, boolean isAutomake) {
        log.debug("Build finished");
        if (Objects.equals(project, currentProject)) {
          buildRunning.set(false);
          fireStateChanged(project);
        }
      }
    });
  }

  @Override
  public void dispose() {
    if (connection != null) {
      connection.disconnect();
      connection = null;
    }
  }
}
