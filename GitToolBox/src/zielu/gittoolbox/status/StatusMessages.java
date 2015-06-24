package zielu.gittoolbox.status;

import com.google.common.collect.Iterables;
import git4idea.repo.GitRepository;
import git4idea.util.GitUIUtil;
import java.util.Map;
import java.util.Map.Entry;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.UtfSeq;
import zielu.gittoolbox.util.GtUtil;
import zielu.gittoolbox.util.Html;

public enum StatusMessages {
    ;

    public static String behindStatus(RevListCount behindCount) {
        switch (behindCount.status()) {
            case Success: {
                if (behindCount.value() > 0) {
                    return behindCount.value() + UtfSeq.arrowDown;
                } else {
                    return ResBundle.getString("message.up.to.date");
                }
            }
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

    private static String prepareSingleLineMessage(RevListCount status) {
        return ": " + behindStatus(status);
    }

    private static String prepareMultiLineMessage(Map<GitRepository, RevListCount> statuses) {
        StringBuilder result = new StringBuilder(":");
        for (Entry<GitRepository, RevListCount> status : statuses.entrySet()) {
            result.append(Html.br)
                  .append(GitUIUtil.bold(GtUtil.name(status.getKey())))
                  .append(": ")
                  .append(behindStatus(status.getValue()));
        }
        return result.toString();
    }

    public static String prepareBehindMessage(Map<GitRepository, RevListCount> statuses) {
        StringBuilder message = new StringBuilder(ResBundle.getString("message.fetch.done"));
        if (statuses.size() == 1) {
            message.append(prepareSingleLineMessage(Iterables.getOnlyElement(statuses.values())));
        } else {
            message.append(prepareMultiLineMessage(statuses));
        }
        return message.toString();
    }
}
