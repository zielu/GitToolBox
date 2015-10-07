package zielu.gittoolbox.ui;

import com.google.common.collect.Iterables;
import com.intellij.openapi.components.ServiceManager;
import git4idea.repo.GitRepository;
import git4idea.util.GitUIUtil;
import java.util.Map;
import java.util.Map.Entry;
import zielu.gittoolbox.GitToolBoxConfig;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.status.GitAheadBehindCount;
import zielu.gittoolbox.status.RevListCount;
import zielu.gittoolbox.status.Status;
import zielu.gittoolbox.util.GtUtil;
import zielu.gittoolbox.util.Html;

public class StatusMessages {

    public static StatusMessages getInstance() {
        return ServiceManager.getService(StatusMessages.class);
    }

    private StatusPresenter presenter() {
        return GitToolBoxConfig.getInstance().getPresenter();
    }

    public String behindStatus(RevListCount behindCount) {
        switch (behindCount.status()) {
            case Success: {
                if (behindCount.value() > 0) {
                    return presenter().behindStatus(behindCount.value());
                } else {
                    return ResBundle.getString("message.up.to.date");
                }
            }
            default: {
                return commonStatus(behindCount.status());
            }
        }
    }

    public String aheadBehindStatus(GitAheadBehindCount count) {
        switch (count.status()) {
            case Success: {
                if (count.isNotZero()) {
                    return presenter().aheadBehindStatus(count.ahead.value(), count.behind.value());
                } else {
                    return ResBundle.getString("message.up.to.date");
                }
            }
            default: {
                return commonStatus(count.status());
            }
        }
    }

    private String commonStatus(Status status) {
        switch (status) {
            case Cancel: {
                return ResBundle.getString("message.cancelled");
            }
            case Failure: {
                return ResBundle.getString("message.failure");
            }
            case NoRemote: {
                return ResBundle.getString("message.no.remote");
            }
            default: {
                return ResBundle.getString("message.unknown");
            }
        }
    }

    private String repoNamePrefix(GitRepository repository) {
        return GitUIUtil.code(GtUtil.name(repository) + ": ");
    }

    private String prepareSingleLineMessage(GitRepository repository, RevListCount status, boolean forceRepoName) {
        String message = "";
        if (forceRepoName) {
            message += repoNamePrefix(repository);
        }
        message += behindStatus(status);
        return message;
    }

    private String prepareMultiLineMessage(Map<GitRepository, RevListCount> statuses) {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Entry<GitRepository, RevListCount> status : statuses.entrySet()) {
            if (!first) {
                result.append(Html.br);
            } else {
                first = false;
            }
            result.append(repoNamePrefix(status.getKey()))
                  .append(behindStatus(status.getValue()));
        }
        return result.toString();
    }

    public String prepareBehindMessage(Map<GitRepository, RevListCount> statuses) {
        return prepareBehindMessage(statuses, false);
    }

    public String prepareBehindMessage(Map<GitRepository, RevListCount> statuses, boolean forceRepoNames) {
        StringBuilder message = new StringBuilder();
        if (statuses.size() == 1) {
            Entry<GitRepository, RevListCount> entry = Iterables.getOnlyElement(statuses.entrySet());
            message.append(prepareSingleLineMessage(entry.getKey(), entry.getValue(), forceRepoNames));
        } else {
            message.append(prepareMultiLineMessage(statuses));
        }
        return message.toString();
    }
}
