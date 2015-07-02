package zielu.gittoolbox.status;

import com.google.common.base.Optional;
import com.intellij.openapi.util.Key;
import git4idea.commands.GitLineHandlerListener;

public class GitRevListLeftRightCounter implements GitLineHandlerListener {
    private int ahead = 0;
    private int behind = 0;

    private int exitCode;
    private Optional<Throwable> error = Optional.absent();

    @Override
    public void onLineAvailable(String line, Key outputType) {
        if (line.startsWith("<")) {
            ahead++;
        } else if (line.startsWith(">")) {
            behind++;
        }
    }

    public int ahead() {
        return ahead;
    }

    public int behind() {
        return behind;
    }

    public boolean isSuccess() {
        return exitCode == 0 && !error.isPresent();
    }

    @Override
    public void processTerminated(int exitCode) {
        this.exitCode = exitCode;
    }

    @Override
    public void startFailed(Throwable throwable) {
        error = Optional.of(throwable);
    }
}
