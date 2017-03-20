package zielu.gittoolbox.util;

import com.google.common.base.Stopwatch;
import com.intellij.openapi.diagnostic.Logger;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class LogWatch {
    private final Logger myLog;
    private final String myMessage;
    private final boolean myEnabled;
    private final Stopwatch myWatch;

    private LogWatch(Logger log, String message) {
        myLog = log;
        myEnabled = myLog.isDebugEnabled();
        if (myEnabled) {
            myMessage = message;
            myWatch = Stopwatch.createUnstarted();
        } else {
            myMessage = "";
            myWatch = null;
        }
    }

    public static LogWatch create(Logger log, String message) {
        return new LogWatch(log, message);
    }

    public LogWatch start() {
        if (myEnabled) {
            myWatch.start();
        }
        return this;
    }

    public LogWatch elapsed(String message, Object... rest) {
        if (myEnabled) {
            String other = Arrays.stream(rest).map(String::valueOf).collect(Collectors.joining(""));
            myLog.debug(myMessage + "/" + message +  other +  " [ms]: ", myWatch.elapsed(TimeUnit.MILLISECONDS));
        }
        return this;
    }

    public LogWatch finish() {
        if (myEnabled) {
            myLog.debug(myMessage + " [ms]: ", myWatch.stop().elapsed(TimeUnit.MILLISECONDS));
        }
        return this;
    }
}
