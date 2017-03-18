package zielu.gittoolbox.fetch;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import zielu.gittoolbox.extension.AutoFetchAllowed;

import java.util.concurrent.atomic.AtomicBoolean;

public class AutoFetchAllowedDumbMode implements AutoFetchAllowed {
    private final Logger LOG = Logger.getInstance(getClass());

    private final AtomicBoolean myInDumbMode = new AtomicBoolean();
    private MessageBusConnection myConnection;

    @Override
    public boolean isAllowed() {
        return !myInDumbMode.get();
    }

    @Override
    public void initialize(Project myProject) {
        myConnection = myProject.getMessageBus().connect();
        myConnection.subscribe(DumbService.DUMB_MODE, new DumbService.DumbModeListener() {
            @Override
            public void enteredDumbMode() {
                LOG.debug("Enter dumb mode");
                myInDumbMode.set(true);
            }

            @Override
            public void exitDumbMode() {
                LOG.debug("Exit dumb mode");
                myInDumbMode.set(false);
                fireStateChanged(myProject);
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
