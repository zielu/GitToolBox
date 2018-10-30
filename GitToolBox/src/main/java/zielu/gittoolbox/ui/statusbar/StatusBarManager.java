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
    connection.subscribe(ConfigNotifier.CONFIG_TOPIC, new ConfigNotifier.Adapter() {
      @Override
      public void configChanged(GitToolBoxConfig2 config) {
        SwingUtilities.invokeLater(() -> {
          if (opened.get()) {
            setVisible(config.showStatusWidget, config.showBlame);
          }
        });
      }
    });
  }

  private void uninstall() {
    setVisible(false, false);
  }

  @Override
  public void opened() {
    if (opened.compareAndSet(false, true)) {
      if (hasUi()) {
        statusWidget = GitStatusWidget.create(project);
        statusWidget.opened();
        blameWidget = new BlameStatusWidget(project);
        blameWidget.opened();
        install();

        GitToolBoxConfig2 config = GitToolBoxConfig2.getInstance();
        setVisible(config.showStatusWidget, config.showBlame);
      }
    }
  }

  private void setVisible(boolean showStatusWidget, boolean showBlame) {
    StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
    if (statusBar != null) {
      if (showStatusWidget) {
        if (statusWidget == null) {
          statusWidget = GitStatusWidget.create(project);
          statusWidget.opened();
        }
        statusBar.addWidget(statusWidget, project);
        statusWidget.setVisible(true);
      } else {
        if (statusWidget != null) {
          statusBar.removeWidget(statusWidget.ID());
          statusWidget.closed();
          statusWidget = null;
        }
      }

      if (showBlame) {
        if (blameWidget == null) {
          blameWidget = new BlameStatusWidget(project);
          blameWidget.opened();
        }
        statusBar.addWidget(blameWidget, project);
        blameWidget.setVisible(true);
      } else {
        if (blameWidget != null) {
          statusBar.removeWidget(blameWidget.ID());
          blameWidget.closed();
          blameWidget = null;
        }
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
