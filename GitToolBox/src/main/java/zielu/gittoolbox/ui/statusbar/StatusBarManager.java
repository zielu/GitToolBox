package zielu.gittoolbox.ui.statusbar;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
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
    connection.subscribe(ConfigNotifier.CONFIG_TOPIC, new ConfigNotifier() {
      @Override
      public void configChanged(GitToolBoxConfig2 previous, GitToolBoxConfig2 current) {
        SwingUtilities.invokeLater(() -> {
          if (opened.get()) {
            setVisible(current.showStatusWidget, current.showBlame);
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
        setVisible(statusBar, statusWidget, true);
      } else {
        if (statusWidget != null) {
          setVisible(statusBar, statusWidget, false);
          statusWidget = null;
        }
      }

      if (showBlame) {
        if (blameWidget == null) {
          blameWidget = new BlameStatusWidget(project);
          blameWidget.opened();
        }
        setVisible(statusBar, blameWidget, true);
      } else {
        if (blameWidget != null) {
          setVisible(statusBar, blameWidget, false);
          blameWidget = null;
        }
      }
    }
  }

  private <T extends StatusBarWidget & StatusBarUi> void setVisible(StatusBar statusBar, T widget, boolean visible) {
    if (visible) {
      if (statusBar.getWidget(widget.ID()) == null) {
        statusBar.addWidget(widget, project);
      }
      widget.setVisible(true);
    } else {
      widget.setVisible(false);
      statusBar.removeWidget(widget.ID());
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
