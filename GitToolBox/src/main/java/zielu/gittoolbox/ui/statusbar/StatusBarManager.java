package zielu.gittoolbox.ui.statusbar;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.util.messages.MessageBusConnection;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.ProjectAware;
import zielu.gittoolbox.config.ConfigNotifier;
import zielu.gittoolbox.config.GitToolBoxConfig2;

public class StatusBarManager implements Disposable, ProjectAware {
  private final AtomicBoolean opened = new AtomicBoolean();
  private final Project project;
  private final MessageBusConnection connection;
  private GitStatusWidget statusWidget;
  private BlameStatusWidget blameWidget;

  private StatusBarManager(Project project) {
    this.project = project;
    connection = this.project.getMessageBus().connect();
  }

  @NotNull
  public static StatusBarManager create(@NotNull Project project) {
    return new StatusBarManager(project);
  }

  private void install() {
    StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
    if (statusBar != null) {
      statusBar.addWidget(statusWidget, project);
      statusWidget.installed();
      statusBar.addWidget(blameWidget, project);
      connection.subscribe(ConfigNotifier.CONFIG_TOPIC, new ConfigNotifier.Adapter() {
        @Override
        public void configChanged(GitToolBoxConfig2 config) {
          final boolean showStatusWidget = config.showStatusWidget;
          final boolean showLensBlame = config.showBlame;
          SwingUtilities.invokeLater(() -> {
            if (opened.get()) {
              statusWidget.setVisible(showStatusWidget);
              blameWidget.setVisible(showLensBlame);
            }
          });
        }
      });
    }
  }

  private void uninstall() {
    StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
    if (statusBar != null) {
      if (statusWidget != null) {
        statusBar.removeWidget(statusWidget.ID());
        statusWidget.uninstalled();
        statusWidget = null;
      }
      if (blameWidget != null) {
        statusBar.removeWidget(blameWidget.ID());
        blameWidget = null;
      }
    }
  }

  @Override
  public void opened() {
    if (opened.compareAndSet(false, true)) {
      if (hasUi()) {
        statusWidget = GitStatusWidget.create(project);
        blameWidget = new BlameStatusWidget(project);
        install();
        GitToolBoxConfig2 config = GitToolBoxConfig2.getInstance();
        statusWidget.setVisible(config.showStatusWidget);
        blameWidget.setVisible(config.showBlame);
      }
    }
  }

  @Override
  public void closed() {
    if (opened.compareAndSet(true, false)) {
      cleanUp();
    }
  }

  private boolean hasUi() {
    return !ApplicationManager.getApplication().isHeadlessEnvironment();
  }

  private void cleanUp() {
    connection.disconnect();
    if (hasUi()) {
      uninstall();
    }
  }

  @Override
  public void dispose() {
  }
}
