package zielu.gittoolbox.status;

import com.intellij.openapi.util.Key;
import git4idea.commands.GitLineHandlerListener;

public class GitRevListCounter implements GitLineHandlerListener {
    private int count = -1;
    @Override
    public void onLineAvailable(String line, Key outputType) {
        count = Integer.parseInt(line);
    }                             

    @Override
    public void processTerminated(int exitCode) {
    }

    @Override
    public void startFailed(Throwable exception) {
    }
    
    public int count() {
        return count;
    }
}
