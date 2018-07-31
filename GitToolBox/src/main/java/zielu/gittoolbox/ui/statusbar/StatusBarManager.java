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
      connection.subscribe(ConfigNotifier.CONFIG_TOPIC, new ConfigNotifier.Adapter() {
        @Override
        public void configChanged(GitToolBoxConfig2 config) {
          final boolean showStatusWidget = config.showStatusWidget;
          SwingUtilities.invokeLater(() -> {
            if (opened.get()) {
              statusWidget.setVisible(showStatusWidget);
            }
          });
        }
      });
    }
  }

  private void uninstall() {
    StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
    if (statusBar != null) {
      statusBar.removeWidget(statusWidget.ID());
      statusWidget.uninstalled();
    }
  }

  @Override
  public void opened() {
    if (opened.compareAndSet(false, true)) {
      if (hasUi()) {
        statusWidget = GitStatusWidget.create(project);
        install();
        statusWidget.setVisible(GitToolBoxConfig2.getInstance().showStatusWidget);
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
      if (statusWidget != null) {
        uninstall();
        statusWidget = null;
      }
    }
  }

  @Override
  public void dispose() {
  }
}
