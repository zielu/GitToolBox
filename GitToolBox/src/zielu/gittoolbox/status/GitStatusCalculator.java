package zielu.gittoolbox.status;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
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
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.NotNull;

public class GitStatusCalculator {
    private final Project project;
    private final ProgressIndicator indicator;

    private GitStatusCalculator(Project _project, ProgressIndicator _indicator) {
        project = Preconditions.checkNotNull(_project);
        indicator = Preconditions.checkNotNull(_indicator);
    }

    public static GitStatusCalculator create(@NotNull Project project, @NotNull ProgressIndicator indicator) {
        return new GitStatusCalculator(project, indicator);
    }

    public List<GitAheadBehindStatus> aheadBehindStatus(Collection<GitRepository> repositories) {
        List<GitAheadBehindStatus> result = Lists.newArrayListWithCapacity(repositories.size());
        for (GitRepository repository : repositories){
            result.add(aheadBehindStatus(repository));
        }
        return result;
    }

    public List<Integer> behindStatus(Collection<GitRepository> repositories){
        List<Integer> result = Lists.newArrayListWithCapacity(repositories.size());
        for (GitRepository repository : repositories){
            result.add(behindStatus(repository));
        }
        return result;
    }

    private int behindStatus(GitRepository repository) {
        Optional<GitBranchTrackInfo> trackInfo = trackInfoForCurrentBranch(repository);
        if (trackInfo.isPresent()) {
            return behindStatus(repository.getCurrentBranch(), trackInfo.get(), repository);
        }
        return 0;
    }

    private GitAheadBehindStatus aheadBehindStatus(GitRepository repository) {
        Optional<GitBranchTrackInfo> trackInfo = trackInfoForCurrentBranch(repository);
        if (trackInfo.isPresent()) {
            return aheadBehindStatus(repository.getCurrentBranch(), trackInfo.get(), repository);
        }
        return GitAheadBehindStatus.empty();
    }

    private int behindStatus(GitLocalBranch currentBranch, GitBranchTrackInfo trackInfo, GitRepository repository) {
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
        int behind = behindCount(localName, remoteName, repository);
        int ahead = aheadCount(localName, remoteName, repository);
        return GitAheadBehindStatus.create(ahead, behind);
    }

    private int behindCount(String localName, String remoteName, GitRepository repository) {
        return doRevListCount(localName+".."+remoteName, repository);
    }

    private int aheadCount(String localName, String remoteName, GitRepository repository) {
        return doRevListCount(remoteName+".."+localName, repository);
    }

    private int doRevListCount(String branches, GitRepository repository) {
        final GitLineHandler handler = new GitLineHandler(project, repository.getRoot(), GitCommand.REV_LIST);
        handler.addParameters(branches, "--count");
        final GitRevListCounter counter = new GitRevListCounter();
        handler.addLineListener(counter);
        GitTask task = new GitTask(project, handler, branches);
        task.setProgressIndicator(indicator);
        final AtomicReference<Integer> result = new AtomicReference<Integer>();
        task.execute(true, false, new GitTaskResultHandlerAdapter() {
            @Override
            protected void onSuccess() {
                result.set(counter.count());
            }

            @Override
            protected void onCancel() {
                result.set(-1);
            }

            @Override
            protected void onFailure() {
                result.set(-1);
            }
        });
        return result.get();
    }
}
