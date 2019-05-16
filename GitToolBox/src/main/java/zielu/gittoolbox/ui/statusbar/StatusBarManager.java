package zielu.gittoolbox.ui.statusbar;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.util.messages.MessageBusConnection;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.config.ConfigNotifier;
import zielu.gittoolbox.config.GitToolBoxConfig2;
import zielu.gittoolbox.ui.util.AppUiUtil;

class StatusBarManager implements ProjectComponent {
  private final AtomicBoolean opened = new AtomicBoolean();
  private final Project project;
  private final MessageBusConnection connection;
  private GitStatusWidget statusWidget;
  private BlameStatusWidget blameWidget;

  StatusBarManager(@NotNull Project project) {
    this.project = project;
    connection = this.project.getMessageBus().connect(project);
  }

  private void install() {
    connection.subscribe(ConfigNotifier.CONFIG_TOPIC, new ConfigNotifier() {
      @Override
      public void configChanged(GitToolBoxConfig2 previous, GitToolBoxConfig2 current) {
        AppUiUtil.invokeLater(project, () -> {
          if (opened.get()) {
            setVisible(current.showStatusWidget, current.showBlameWidget);
          }
        });
      }
    });
  }

  private void uninstall() {
    setVisible(false, false);
  }

  @Override
  public void projectOpened() {
    if (opened.compareAndSet(false, true) && hasUi()) {
      statusWidget = GitStatusWidget.create(project);
      statusWidget.opened();
      blameWidget = new BlameStatusWidget(project);
      install();

      GitToolBoxConfig2 config = GitToolBoxConfig2.getInstance();
      setVisible(config.showStatusWidget, config.showBlameWidget);
    }
  }

  private void setVisible(boolean showStatusWidget, boolean showBlameWidget) {
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
          statusWidget.closed();
          statusWidget = null;
        }
      }
      if (showBlameWidget) {
        if (blameWidget == null) {
          blameWidget = new BlameStatusWidget(project);
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
  public void projectClosed() {
    if (opened.compareAndSet(true, false)) {
      cleanUp();
    }
  }

  private boolean hasUi() {
    return !ApplicationManager.getApplication().isHeadlessEnvironment();
  }

  private void cleanUp() {
    if (hasUi()) {
      uninstall();
    }
  }
}
