package zielu.gittoolbox.ui.statusbar;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.util.messages.MessageBusConnection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;
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
    GitToolBoxConfig2 config = GitToolBoxConfig2.getInstance();
    updateWidgets(config.showStatusWidget, config.showBlameWidget);

    connection.subscribe(ConfigNotifier.CONFIG_TOPIC, new ConfigNotifier() {
      @Override
      public void configChanged(GitToolBoxConfig2 previous, GitToolBoxConfig2 current) {
        AppUiUtil.invokeLater(project, () -> {
          if (opened.get()) {
            updateWidgets(current.showStatusWidget, current.showBlameWidget);
          }
        });
      }
    });
  }

  private void uninstall() {
    updateWidgets(false, false);
  }

  @Override
  public void projectOpened() {
    if (opened.compareAndSet(false, true) && hasUi()) {
      install();
    }
  }

  private void updateWidgets(boolean showStatusWidget, boolean showBlameWidget) {
    StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
    if (statusBar != null) {
      statusWidget = presentWidget(() -> statusWidget, GitStatusWidget::create, statusBar, showStatusWidget);
      blameWidget = presentWidget(() -> blameWidget, BlameStatusWidget::new, statusBar, showBlameWidget);
    }
  }

  private <T extends StatusBarWidget & StatusBarUi> T presentWidget(Supplier<T> current, Function<Project, T> creator,
                                                                    StatusBar statusBar, boolean state) {
    if (state) {
      T instance = createWidget(current, creator);
      setVisible(statusBar, instance, state);
      return instance;
    } else {
      T instance = current.get();
      if (instance != null) {
        setVisible(statusBar, instance, state);
        instance.closed();
      }
      return null;
    }
  }

  private <T extends StatusBarWidget & StatusBarUi> T createWidget(Supplier<T> current, Function<Project, T> creator) {
    T instance = current.get();
    if (instance == null) {
      instance = creator.apply(project);
      instance.opened();
    }
    return instance;
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
