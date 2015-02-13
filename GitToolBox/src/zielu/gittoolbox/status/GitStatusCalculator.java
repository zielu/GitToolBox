package zielu.gittoolbox.status;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import git4idea.GitLocalBranch;
import git4idea.GitUtil;
import git4idea.commands.GitCommand;
import git4idea.commands.GitLineHandler;
import git4idea.commands.GitTask;
import git4idea.commands.GitTaskResultHandlerAdapter;
import git4idea.repo.GitBranchTrackInfo;
import git4idea.repo.GitRepository;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.NotNull;

public class GitStatusCalculator {
    private final Project myProject;
    private final ProgressIndicator myIndicator;

    private GitStatusCalculator(Project project, ProgressIndicator indicator) {
        myProject = Preconditions.checkNotNull(project);
        myIndicator = Preconditions.checkNotNull(indicator);
    }

    public static GitStatusCalculator create(@NotNull Project project, @NotNull ProgressIndicator indicator) {
        return new GitStatusCalculator(project, indicator);
    }

    public Map<GitRepository, RevListCount> behindStatus(Collection<GitRepository> repositories){
        Map<GitRepository, RevListCount> result = Maps.newLinkedHashMap();
        for (GitRepository repository : repositories){
            result.put(repository, behindStatus(repository));
        }
        return result;
    }

    private RevListCount behindStatus(GitRepository repository) {
        Optional<GitBranchTrackInfo> trackInfo = trackInfoForCurrentBranch(repository);
        if (trackInfo.isPresent()) {
            return behindStatus(repository.getCurrentBranch(), trackInfo.get(), repository);
        }
        return RevListCount.noRemote();
    }

    private GitAheadBehindStatus aheadBehindStatus(GitRepository repository) {
        Optional<GitBranchTrackInfo> trackInfo = trackInfoForCurrentBranch(repository);
        if (trackInfo.isPresent()) {
            return aheadBehindStatus(repository.getCurrentBranch(), trackInfo.get(), repository);
        }
        return GitAheadBehindStatus.noRemote();
    }

    private RevListCount behindStatus(GitLocalBranch currentBranch, GitBranchTrackInfo trackInfo, GitRepository repository) {
        String localName = currentBranch.getName();
        String remoteName = trackInfo.getRemoteBranch().getNameForLocalOperations();
        return behindCount(localName, remoteName, repository);
    }

    private Optional<GitBranchTrackInfo> trackInfoForCurrentBranch(GitRepository repository) {
        GitBranchTrackInfo trackInfo = GitUtil.getTrackInfoForCurrentBranch(repository);
        return Optional.fromNullable(trackInfo);
    }

    private GitAheadBehindStatus aheadBehindStatus(
        GitLocalBranch localBranch, GitBranchTrackInfo trackInfo, GitRepository repository) {
        String localName = localBranch.getName();
        String remoteName = trackInfo.getRemoteBranch().getNameForLocalOperations();
        RevListCount behind = behindCount(localName, remoteName, repository);
        RevListCount ahead = aheadCount(localName, remoteName, repository);
        return GitAheadBehindStatus.create(ahead, behind);
    }

    private RevListCount behindCount(String localName, String remoteName, GitRepository repository) {
        return doRevListCount(localName+".."+remoteName, repository);
    }

    private RevListCount aheadCount(String localName, String remoteName, GitRepository repository) {
        return doRevListCount(remoteName+".."+localName, repository);
    }

    private RevListCount doRevListCount(String branches, GitRepository repository) {
        final GitLineHandler handler = new GitLineHandler(myProject, repository.getRoot(), GitCommand.REV_LIST);
        handler.addParameters(branches, "--count");
        final GitRevListCounter counter = new GitRevListCounter();
        handler.addLineListener(counter);
        GitTask task = new GitTask(myProject, handler, branches);
        task.setProgressIndicator(myIndicator);
        final AtomicReference<RevListCount> result = new AtomicReference<RevListCount>();
        task.execute(true, false, new GitTaskResultHandlerAdapter() {
            @Override
            protected void onSuccess() {
                result.set(RevListCount.success(counter.count()));
            }

            @Override
            protected void onCancel() {
                result.set(RevListCount.cancel());
            }

            @Override
            protected void onFailure() {
                result.set(RevListCount.failure());
            }
        });
        return Preconditions.checkNotNull(result.get(), "Null rev list count");
    }
}
