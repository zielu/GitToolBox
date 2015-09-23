package zielu.gittoolbox.fetch;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import zielu.gittoolbox.GitToolBoxConfig;
import zielu.gittoolbox.GitToolBoxConfigNotifier;
import zielu.gittoolbox.ProjectAware;

public class AutoFetch implements Disposable, ProjectAware {
    private final Logger LOG = Logger.getInstance(getClass());

    private final Project myProject;
    private final MessageBusConnection myConnection;
    private final ScheduledExecutorService myExecutor = Executors.newSingleThreadScheduledExecutor(
        new ThreadFactoryBuilder().setDaemon(true).setNameFormat("AutoFetch-%s").build()
    );

    private ScheduledFuture<?> myScheduledTask;
    private int currentInterval;

    private AutoFetch(Project project) {
        myProject = project;
        myConnection = myProject.getMessageBus().connect(this);
        myConnection.subscribe(GitToolBoxConfigNotifier.CONFIG_TOPIC, new GitToolBoxConfigNotifier() {
            @Override
            public void configChanged(GitToolBoxConfig config) {
                if (config.autoFetch) {
                    LOG.debug("Auto-fetch enabled");
                    synchronized (AutoFetch.this) {
                        if (currentInterval != config.autoFetchIntervalMinutes) {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Auto-fetch interval or state changed: enabled="
                                    +config.autoFetch+", interval="+config.autoFetchIntervalMinutes);
                            }

                            if (myScheduledTask != null) {
                                LOG.debug("Existing task cancelled on auto-fetch change");
                                myScheduledTask.cancel(false);
                            }
                            currentInterval = config.autoFetchIntervalMinutes;
                            myScheduledTask = scheduleTask(config.autoFetchIntervalMinutes);
                        } else {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Auto-fetch interval and state did not change: enabled="
                                    +config.autoFetch+", interval="+config.autoFetchIntervalMinutes);
                            }
                        }
                    }
                } else {
                    LOG.debug("Auto-fetch disabled");
                    synchronized (AutoFetch.this) {
                        if (myScheduledTask != null) {
                            LOG.debug("Existing task cancelled on auto-fetch disable");
                            myScheduledTask.cancel(false);
                            myScheduledTask = null;
                        }
                    }
                }
            }
        });
    }

    private ScheduledFuture<?> scheduleFirstTask(long intervalMinutes) {
        return myExecutor.scheduleWithFixedDelay(AutoFetchTask.create(myProject),
            30,
            TimeUnit.MINUTES.toSeconds(intervalMinutes),
            TimeUnit.SECONDS);
    }

    private ScheduledFuture<?> scheduleTask(long intervalMinutes) {
        return myExecutor.scheduleWithFixedDelay(AutoFetchTask.create(myProject),
            intervalMinutes,
            intervalMinutes,
            TimeUnit.MINUTES);
    }

    public static AutoFetch create(Project project) {
        return new AutoFetch(project);
    }

    @Override
    public void dispose() {
        myConnection.disconnect();
    }

    @Override
    public void opened() {
        GitToolBoxConfig config = GitToolBoxConfig.getInstance();
        if (config.autoFetch) {
            LOG.debug("Scheduling auto-fetch on project open");
            synchronized (this) {
                currentInterval = config.autoFetchIntervalMinutes;
                myScheduledTask = scheduleFirstTask(config.autoFetchIntervalMinutes);
            }
        }
    }

    @Override
    public void closed() {
        myExecutor.shutdownNow();
        synchronized (this) {
            myScheduledTask = null;
        }
    }
}
