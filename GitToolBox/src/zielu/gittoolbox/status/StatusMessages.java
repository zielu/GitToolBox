package zielu.gittoolbox.status;

import git4idea.repo.GitRepository;
import java.util.Collection;
import java.util.List;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.UtfSeq;

public enum StatusMessages {
    instace;

    public static String prepareMessage(Collection<GitRepository> repositories, List<GitAheadBehindStatus> statuses) {
        StringBuilder message = new StringBuilder(ResBundle.getString("message.fetch.success")+":");
        if (statuses.size() == 1) {
            message.append(" ").append(statuses.get(0));
        } else {
            int index = 0;
            for (GitRepository repository : repositories) {
                message.append("\n").append(repository.getGitDir().getName()).append(statuses.get(index));
                index++;
            }
        }
        return message.toString();
    }

    public static String prepareBehindMessage(Collection<GitRepository> repositories, List<Integer> statuses) {
        StringBuilder message = new StringBuilder(ResBundle.getString("message.fetch.success"));
        if (statuses.size() == 1) {
            message.append(": ");
            Integer singleStatus = statuses.get(0);
            if (singleStatus > 0) {
                message.append(singleStatus).append(UtfSeq.ArrowDown);
            } else {
                message.append(ResBundle.getString("message.up.to.date"));
            }
        } else {
            message.append(":");
            int index = 0;
            for (GitRepository repository : repositories) {
                message.append("\n").append(repository.getGitDir().getName()).append(statuses.get(index));
                index++;
            }
        }
        return message.toString();
    }
}
