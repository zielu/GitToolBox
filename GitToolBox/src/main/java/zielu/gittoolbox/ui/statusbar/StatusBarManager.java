package zielu.gittoolbox.ui.statusbar;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.util.messages.MessageBusConnection;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.ProjectAware;
import zielu.gittoolbox.config.ConfigNotifier;
import zielu.gittoolbox.config.GitToolBoxConfig;

public class StatusBarManager implements Disposable, ProjectAware {
  private final AtomicBoolean opened = new AtomicBoolean();
  private final Project project;
  private final MessageBusConnection connection;
  private GitStatusWidget statusWidget;

  private StatusBarManager(Project project) {
    this.project = project;
    connection = this.project.getMessageBus().connect();
    connection.subscribe(ConfigNotifier.CONFIG_TOPIC, new ConfigNotifier.Adapter() {
      @Override
      public void configChanged(GitToolBoxConfig config) {
        final boolean showStatusWidget = config.showStatusWidget;
        SwingUtilities.invokeLater(() -> {
          if (opened.get()) {
            statusWidget.setVisible(showStatusWidget);
          }
        });
      }
    });
  }

  @SuppressFBWarnings({"NP_NULL_PARAM_DEREF"})
  @NotNull
  public static StatusBarManager create(@NotNull Project project) {
    return new StatusBarManager(project);
  }

  private void install() {
    StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
    if (statusBar != null) {
      statusBar.addWidget(statusWidget, project);
      statusWidget.installed();
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
      if (!ApplicationManager.getApplication().isHeadlessEnvironment()) {
        statusWidget = GitStatusWidget.create(project);
        install();
        statusWidget.setVisible(GitToolBoxConfig.getInstance().showStatusWidget);
      }
    }
  }

  @Override
  public void closed() {
    opened.compareAndSet(true, false);
  }

  @Override
  public void dispose() {
    if (!ApplicationManager.getApplication().isHeadlessEnvironment()) {
      if (statusWidget != null) {
        uninstall();
        statusWidget = null;
      }
    }
    connection.disconnect();
  }
}
