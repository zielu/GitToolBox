package zielu.gittoolbox.util;

import com.google.common.base.Stopwatch;
import com.intellij.openapi.diagnostic.Logger;
import java.util.concurrent.TimeUnit;

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

    public LogWatch elapsed(String message) {
        if (myEnabled) {
            myLog.debug(myMessage + "/" + message + " [ms]: ", myWatch.elapsed(TimeUnit.MILLISECONDS));
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
