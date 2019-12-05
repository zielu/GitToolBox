package zielu.gittoolbox.ui.statusbar;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBar.Anchors;
import com.intellij.openapi.wm.StatusBar.StandardWidgets;
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
    updateWidgets(config.isStatusBarWidgetVisible(), config.showBlameWidget);

    connection.subscribe(ConfigNotifier.CONFIG_TOPIC, new ConfigNotifier() {
      @Override
      public void configChanged(GitToolBoxConfig2 previous, GitToolBoxConfig2 current) {
        AppUiUtil.invokeLater(project, () -> {
          if (opened.get()) {
            updateWidgets(current.isStatusBarWidgetVisible(), current.showBlameWidget);
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
      String statusAnchor = getStatusAnchor(showStatusWidget, showBlameWidget);
      statusWidget = presentWidget(() -> statusWidget, GitStatusWidget::create, statusBar,
          statusAnchor, showStatusWidget);
      String blameAnchor = getBlameAnchor(showStatusWidget, showBlameWidget);
      blameWidget = presentWidget(() -> blameWidget, BlameStatusWidget::new, statusBar,
          blameAnchor, showBlameWidget);
    }
  }

  private String getStatusAnchor(boolean showStatusWidget, boolean showBlameWidget) {
    if (showStatusWidget) {
      return Anchors.after(StandardWidgets.ENCODING_PANEL);
    } else {
      return "";
    }
  }

  private String getBlameAnchor(boolean showStatusWidget, boolean showBlameWidget) {
    if (showStatusWidget && showBlameWidget) {
      return Anchors.after(GitStatusWidget.ID);
    } else if (showBlameWidget) {
      return Anchors.after(StandardWidgets.ENCODING_PANEL);
    } else {
      return "";
    }
  }

  private <T extends StatusBarWidget & StatusBarUi> T presentWidget(Supplier<T> current,
                                                                    Function<Project, T> creator,
                                                                    StatusBar statusBar,
                                                                    String anchor,
                                                                    boolean state) {
    if (state) {
      T instance = createWidget(current, creator);
      setVisible(statusBar, anchor, instance, state);
      return instance;
    } else {
      T instance = current.get();
      if (instance != null) {
        setVisible(statusBar, anchor, instance, state);
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

  private <T extends StatusBarWidget & StatusBarUi> void setVisible(StatusBar statusBar,
                                                                    String anchor,
                                                                    T widget,
                                                                    boolean visible) {
    if (visible) {
      if (statusBar.getWidget(widget.ID()) == null) {
        statusBar.addWidget(widget, anchor, project);
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
