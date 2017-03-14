package zielu.gittoolbox.fetch;

import com.intellij.compiler.server.BuildManagerListener;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.DumbService.DumbModeListener;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;

public class AutoFetchState extends AbstractProjectComponent {
    private final Logger LOG = Logger.getInstance(getClass());
    private final AtomicBoolean myInDumbMode = new AtomicBoolean();
    private final AtomicBoolean myBuildRunning = new AtomicBoolean();
    private final AtomicBoolean myFetchRunning = new AtomicBoolean();
    private MessageBusConnection myConnection;

    public AutoFetchState(Project project) {
        super(project);
    }

    public static AutoFetchState getInstance(@NotNull Project project) {
        return project.getComponent(AutoFetchState.class);
    }

    @Override
    public void initComponent() {
        myConnection = myProject.getMessageBus().connect();
        myConnection.subscribe(DumbService.DUMB_MODE, new DumbModeListener() {
            @Override
            public void enteredDumbMode() {
                LOG.debug("Enter dumb mode");
                myInDumbMode.set(true);
            }

            @Override
            public void exitDumbMode() {
                LOG.debug("Exit dumb mode");
                myInDumbMode.set(false);
                fireStateChanged();
            }
        });
        myConnection.subscribe(BuildManagerListener.TOPIC, new BuildManagerListener() {
            @Override
            public void buildStarted(Project project, UUID sessionId, boolean isAutomake) {
                LOG.debug("Build start");
                if (myProject.equals(project)) {
                    myBuildRunning.set(true);
                }
            }

            @Override
            public void buildFinished(Project project, UUID sessionId, boolean isAutomake) {
                LOG.debug("Build finished");
                if (myProject.equals(project)) {
                    myBuildRunning.set(false);
                    fireStateChanged();
                }
            }
        });
    }

    private void fireStateChanged() {
        myProject.getMessageBus().syncPublisher(AutoFetchNotifier.TOPIC).stateChanged(this);
    }

    @Override
    public void disposeComponent() {
        if (myConnection != null) {
            myConnection.disconnect();
            myConnection = null;
        }
    }

    public boolean canAutoFetch() {
        return !myInDumbMode.get() && !myBuildRunning.get() && !myFetchRunning.get();
    }

    public boolean fetchStart() {
        return myFetchRunning.compareAndSet(false, true);
    }

    public void fetchFinish() {
        myFetchRunning.compareAndSet(true, false);
    }
}
