package zielu.gittoolbox.status;

import git4idea.repo.GitRepository;
import java.util.Collection;
import java.util.List;

public enum StatusMessages {
    instace;

    public static String prepareMessage(Collection<GitRepository> repositories, List<GitAheadBehindStatus> statuses) {
        StringBuilder message = new StringBuilder("Fetched successfully: ");
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
        StringBuilder message = new StringBuilder("Fetched successfully: ");
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
}
