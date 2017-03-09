package zielu.gittoolbox.fetch;

import java.util.concurrent.atomic.AtomicBoolean;

public class AutoFetchState {
    private boolean myInDumbMode ;
    private boolean myBuildRunning;
    private final AtomicBoolean myFetchInProgress = new AtomicBoolean();

    public synchronized void enterDumbMode() {
        myInDumbMode = true;
    }

    public synchronized boolean exitDumbMode() {
        myInDumbMode = false;
        return canAutoFetch();
    }

    public synchronized void buildStarted() {
        myBuildRunning = true;
    }

    public synchronized boolean buildFinished() {
        myBuildRunning = false;
        return canAutoFetch();
    }

    public synchronized boolean canAutoFetch() {
        return !myInDumbMode && !myBuildRunning;
    }

    public boolean fetchStart() {
        return myFetchInProgress.compareAndSet(false, true);
    }

    public boolean fetchFinish() {
        return myFetchInProgress.compareAndSet(true, false);
    }
}
