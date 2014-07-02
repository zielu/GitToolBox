package zielu.gittoolbox.status;

import git4idea.repo.GitRepository;
import git4idea.util.GitUIUtil;
import java.util.Collection;
import java.util.List;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.UtfSeq;

public enum StatusMessages {
    instace;

    private static String behindStatus(int behindCount) {
        if (behindCount > 0) {
            return behindCount + UtfSeq.ArrowDown;
        } else {
            return ResBundle.getString("message.up.to.date");
        }
    }

    public static String prepareBehindMessage(Collection<GitRepository> repositories, List<Integer> statuses) {
        StringBuilder message = new StringBuilder(ResBundle.getString("message.fetch.success"));
        if (statuses.size() == 1) {
            message.append(": ");
            Integer singleStatus = statuses.get(0);
            message.append(behindStatus(singleStatus));
        } else {
            message.append(":");
            int index = 0;
            for (GitRepository repository : repositories) {
                message.append("<br/>")
                    .append(GitUIUtil.bold(repository.getGitDir().getParent().getName()))
                    .append(": ")
                    .append(behindStatus(statuses.get(index)));
                index++;
            }
        }
        return message.toString();
    }
}
