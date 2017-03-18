package zielu.gittoolbox.fetch;

import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.extension.AutoFetchAllowed;
import zielu.gittoolbox.extension.AutoFetchAllowedEP;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class AutoFetchState extends AbstractProjectComponent {
    private final Logger LOG = Logger.getInstance(getClass());

    private final AtomicBoolean myFetchRunning = new AtomicBoolean();
    private final List<AutoFetchAllowed> myFetchAllowed = new ArrayList<>();
    private MessageBusConnection myConnection;

    public AutoFetchState(Project project) {
        super(project);
    }

    public static AutoFetchState getInstance(@NotNull Project project) {
        return project.getComponent(AutoFetchState.class);
    }

    @Override
    public void initComponent() {
        List<AutoFetchAllowedEP> autoFetchAllowedEPs = Arrays.asList(Extensions.getExtensions(AutoFetchAllowedEP.POINT_NAME));
        myFetchAllowed.addAll(autoFetchAllowedEPs.stream().map(ep -> {
            AutoFetchAllowed fetchAllowed = ep.instantiate();
            fetchAllowed.initialize(myProject);
            LOG.debug("Added auto fetch allowed: ", fetchAllowed);
            return fetchAllowed;
        }).collect(Collectors.toList()));
        myConnection = myProject.getMessageBus().connect();
        myConnection.subscribe(AutoFetchAllowed.TOPIC, allowed -> fireStateChanged());
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
        List<AutoFetchAllowed> allowed = new ArrayList<>(myFetchAllowed);
        myFetchAllowed.clear();
        allowed.forEach(AutoFetchAllowed::dispose);
    }

    private boolean isFetchAllowed() {
        boolean allowed = true;
        for (AutoFetchAllowed fetchAllowed : myFetchAllowed) {
            if (!fetchAllowed.isAllowed()) {
                allowed = false;
                break;
            }
        }
        return allowed;
    }

    public boolean canAutoFetch() {
        return isFetchAllowed() && !myFetchRunning.get();
    }

    public boolean fetchStart() {
        return myFetchRunning.compareAndSet(false, true);
    }

    public void fetchFinish() {
        myFetchRunning.compareAndSet(true, false);
    }
}
