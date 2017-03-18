package zielu.gittoolbox.fetch;

import com.intellij.compiler.server.BuildManagerListener;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import zielu.gittoolbox.extension.AutoFetchAllowed;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class AutoFetchAllowedBuild implements AutoFetchAllowed {
    private final Logger LOG = Logger.getInstance(getClass());

    private final AtomicBoolean myBuildRunning = new AtomicBoolean();
    private MessageBusConnection myConnection;

    @Override
    public boolean isAllowed() {
        return !myBuildRunning.get();
    }

    @Override
    public void initialize(Project myProject) {
        myConnection = myProject.getMessageBus().connect();
        myConnection.subscribe(BuildManagerListener.TOPIC, new BuildManagerListener() {
            @Override
            public void beforeBuildProcessStarted(Project project, UUID sessionId) {}

            @Override
            public void buildStarted(Project project, UUID sessionId, boolean isAutomake) {
                LOG.debug("Build start");
                if (project.equals(myProject)) {
                    myBuildRunning.set(true);
                }
            }

            @Override
            public void buildFinished(Project project, UUID sessionId, boolean isAutomake) {
                LOG.debug("Build finished");
                if (project.equals(myProject)) {
                    myBuildRunning.set(false);
                    fireStateChanged(project);
                }
            }
        });
    }

    @Override
    public void dispose() {
        if (myConnection != null) {
            myConnection.disconnect();
            myConnection = null;
        }
    }
}
