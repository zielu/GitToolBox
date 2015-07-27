package zielu.gittoolbox.ui;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.util.messages.MessageBusConnection;
import javax.swing.SwingUtilities;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.GitToolBoxConfig;
import zielu.gittoolbox.GitToolBoxConfigNotifier;

public class StatusBarManager implements Disposable {
    private final Project myProject;
    private final MessageBusConnection myConnection;
    private GitStatusWidget myStatusWidget;

    private StatusBarManager(Project project) {
        myProject = project;
        myConnection = myProject.getMessageBus().connect(this);
        myConnection.subscribe(GitToolBoxConfigNotifier.CONFIG_TOPIC, new GitToolBoxConfigNotifier() {
            @Override
            public void configChanged(GitToolBoxConfig config) {
                final boolean showStatusWidget = config.showStatusWidget;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        myStatusWidget.setVisible(showStatusWidget);
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
        }
    }

    private void uninstall() {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(myProject);
        if (statusBar != null) {
            statusBar.removeWidget(myStatusWidget.ID());
        }
    }

    public void opened() {
        if (!ApplicationManager.getApplication().isHeadlessEnvironment()) {
            myStatusWidget = GitStatusWidget.create(myProject);
            install();
            myStatusWidget.setVisible(GitToolBoxConfig.getInstance().showStatusWidget);
        }
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
