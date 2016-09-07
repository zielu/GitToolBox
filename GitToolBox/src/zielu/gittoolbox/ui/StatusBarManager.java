package zielu.gittoolbox.ui;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.util.messages.MessageBusConnection;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.GitToolBoxConfig;
import zielu.gittoolbox.GitToolBoxConfigNotifier;
import zielu.gittoolbox.ProjectAware;

public class StatusBarManager implements Disposable, ProjectAware {
    private final AtomicBoolean opened = new AtomicBoolean();
    private final Project myProject;
    private final MessageBusConnection myConnection;
    private GitStatusWidget myStatusWidget;

    private StatusBarManager(Project project) {
        myProject = project;
        myConnection = myProject.getMessageBus().connect();
        myConnection.subscribe(GitToolBoxConfigNotifier.CONFIG_TOPIC, new GitToolBoxConfigNotifier.Adapter() {
            @Override
            public void configChanged(GitToolBoxConfig config) {
                final boolean showStatusWidget = config.showStatusWidget;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (opened.get()) {
                            myStatusWidget.setVisible(showStatusWidget);
                        }
                    }
                });
            }
        });
    }

    public static StatusBarManager create(@NotNull Project project) {
        return new StatusBarManager(project);
    }

    private void install() {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(myProject);
        if (statusBar != null) {
            statusBar.addWidget(myStatusWidget, myProject);
            myStatusWidget.installed();
        }
    }

    private void uninstall() {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(myProject);
        if (statusBar != null) {
            statusBar.removeWidget(myStatusWidget.ID());
            myStatusWidget.uninstalled();
        }
    }

    @Override
    public void opened() {
        if (opened.compareAndSet(false, true)) {
            if (!ApplicationManager.getApplication().isHeadlessEnvironment()) {
                myStatusWidget = GitStatusWidget.create(myProject);
                install();
                myStatusWidget.setVisible(GitToolBoxConfig.getInstance().showStatusWidget);
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
            if (myStatusWidget != null) {
                uninstall();
                myStatusWidget = null;
            }
        }
        myConnection.disconnect();
    }
}
