package zielu.gittoolbox.startup;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.util.ThrowableRunnable;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.lifecycle.ProjectLifecycleNotifier;

public class GitToolBoxStartup implements StartupActivity, DumbAware {
  private final Logger log = Logger.getInstance(getClass());

  @Override
  public void runActivity(@NotNull Project project) {
    ConfigMigrator migrator = new ConfigMigrator();
    boolean migrated = migrator.migrate(project);
    if (migrated) {
      saveAppSettings();
    }
    if (!project.isDefault()) {
      project.getMessageBus().syncPublisher(ProjectLifecycleNotifier.TOPIC).projectReady(project);
    }
  }

  private void saveAppSettings() {
    Application application = ApplicationManager.getApplication();
    if (!application.isUnitTestMode()) {
      log.info("Saving settings");
      try {
        WriteAction.runAndWait((ThrowableRunnable<Exception>) application::saveSettings);
      } catch (Exception exception) {
        log.error("Failed to save settings", exception);
      }
    }
  }
}
