package zielu.gittoolbox.status;

import com.intellij.openapi.util.Key;
import git4idea.commands.GitLineHandlerListener;

public class GitRevListCounter implements GitLineHandlerListener {
    private int count = -1;
    @Override
    public void onLineAvailable(String s, Key key) {
        count = Integer.parseInt(s);
    }                             

    @Override
    public void processTerminated(int i) {
        //throw new Error("Not yet implemented");
    }

    @Override
    public void startFailed(Throwable throwable) {
        //throw new Error("Not yet implemented");
    }
    
    public int count() {
        return count;
    }
}
