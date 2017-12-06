package zielu.gittoolbox.util;

import com.google.common.base.Stopwatch;
import com.intellij.openapi.diagnostic.Logger;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import jodd.util.StringBand;

public class LogWatch {
    private final Logger myLog = Logger.getInstance("#zielu.gittoolbox.perf");
    private final String myMessage;
    private final boolean myEnabled;
    private final Stopwatch myWatch;

    private LogWatch(String message) {
        myEnabled = myLog.isTraceEnabled();
        if (myEnabled) {
            myMessage = message;
            myWatch = Stopwatch.createUnstarted();
        } else {
            myMessage = "";
            myWatch = null;
        }
    }

    public static LogWatch create(String message) {
        return new LogWatch(message);
    }

    public static LogWatch createStarted(String message) {
        return create(message).start();
    }

    public LogWatch start() {
        if (myEnabled && !myWatch.isRunning()) {
            myWatch.start();
        }
        return this;
    }

    public LogWatch elapsed(String message, Object... rest) {
        if (myEnabled) {
            StringBand messageToPrint = new StringBand(myMessage).append("|Elapsed/").append(message);
            Arrays.stream(rest).map(String::valueOf).forEach(messageToPrint::append);
            print(messageToPrint, myWatch.elapsed(TimeUnit.MILLISECONDS));
        }
        return this;
    }

    public void finish() {
        if (myEnabled && myWatch.isRunning()) {
            StringBand messageToPrint = new StringBand(myMessage).append("|Finished");
            print(messageToPrint, myWatch.stop().elapsed(TimeUnit.MILLISECONDS));
        }
    }

    private void print(StringBand message, long millis) {
        if (millis > 0) {
            StringBand messageToLog = message.append(" [th:").append(Thread.currentThread().getName()).append("][ms]: ").append(millis);
            myLog.trace(messageToLog.toString());
        }
    }
}
