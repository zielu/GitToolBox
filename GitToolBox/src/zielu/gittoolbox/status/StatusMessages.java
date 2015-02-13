package zielu.gittoolbox.status;

import git4idea.repo.GitRepository;
import git4idea.util.GitUIUtil;
import java.util.Collection;
import java.util.List;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.UtfSeq;
import zielu.gittoolbox.util.GtUtil;
import zielu.gittoolbox.util.Html;

public enum StatusMessages {
    ;

    private static String behindStatus(RevListCount behindCount) {
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

    public static String prepareBehindMessage(Collection<GitRepository> repositories, List<RevListCount> statuses) {
        StringBuilder message = new StringBuilder(ResBundle.getString("message.fetch.success"));
        if (statuses.size() == 1) {
            message.append(": ");
            RevListCount singleStatus = statuses.get(0);
            message.append(behindStatus(singleStatus));
        } else {
            message.append(":");
            int index = 0;
            for (GitRepository repository : repositories) {
                message.append(Html.br)
                    .append(GitUIUtil.bold(GtUtil.name(repository)))
                    .append(": ")
                    .append(behindStatus(statuses.get(index)));
                index++;
            }
        }
        return message.toString();
    }
}
