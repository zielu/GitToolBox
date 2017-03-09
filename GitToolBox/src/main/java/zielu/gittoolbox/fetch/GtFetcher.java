package zielu.gittoolbox.fetch;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.DumbProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsImplUtil;
import git4idea.GitUtil;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import git4idea.update.GitFetchResult;
import git4idea.update.GitFetcher;
import java.util.Collection;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.compat.Notifier;
import zielu.gittoolbox.util.FetchResult;
import zielu.gittoolbox.util.FetchResultsPerRoot;

public class GtFetcher {
    private final Logger LOG = Logger.getInstance(getClass());

    private final Project myProject;
    private final ProgressIndicator myProgress;
    private final GitFetcher myFetcher;
    private final GitRepositoryManager myRepositoryManager;

    private GtFetcher(@NotNull Project project, @NotNull ProgressIndicator progress, Builder builder) {
        myProject = Preconditions.checkNotNull(project, "Null Project");
        myProgress = progress;
        myFetcher = new GitFetcher(myProject, DumbProgressIndicator.INSTANCE, builder.fetchAll);
        myRepositoryManager = GitUtil.getRepositoryManager(myProject);
    }

    @NotNull
    private String makeAdditionalInfoByRoot(@NotNull Map<VirtualFile, String> additionalInfo) {
        if (additionalInfo.isEmpty()) {
            return "";
        }
        StringBuilder info = new StringBuilder();
        if (myRepositoryManager.moreThanOneRoot()) {
            for (Map.Entry<VirtualFile, String> entry : additionalInfo.entrySet()) {
                info.append(entry.getValue()).append(" in ").append(VcsImplUtil.getShortVcsRootName(myProject, entry.getKey())).append("<br/>");
            }
        } else {
            info.append(additionalInfo.values().iterator().next());
        }
        return info.toString();
    }

    public ImmutableCollection<GitRepository> fetchRoots(@NotNull Collection<GitRepository> repositories) {
        Map<VirtualFile, String> additionalInfos = Maps.newHashMapWithExpectedSize(repositories.size());
        FetchResultsPerRoot errorsPerRoot = new FetchResultsPerRoot();
        ImmutableList.Builder<GitRepository> resultBuilder = ImmutableList.builder();
        final boolean debug = LOG.isDebugEnabled();
        for (GitRepository repository : repositories) {
            if (debug) {
                LOG.debug("Fetching " + repository);
            }
            GitFetchResult result = myFetcher.fetch(repository);
            if (debug) {
                LOG.debug("Fetched " + repository + ": success=" + result.isSuccess() + ", error=" + result.isError());
            }
            String ai = result.getAdditionalInfo();
            if (!StringUtil.isEmptyOrSpaces(ai)) {
                additionalInfos.put(repository.getRoot(), ai);
            }
            if (result.isSuccess()) {
                resultBuilder.add(repository);
            } else {
                errorsPerRoot.add(repository, new FetchResult(result, myFetcher.getErrors()));
            }
        }

        errorsPerRoot.showProblems(Notifier.getInstance(myProject));
        showAdditionalInfos(additionalInfos);

        return resultBuilder.build();
    }

    private void showAdditionalInfos(Map<VirtualFile, String> additionalInfos) {
        String additionalInfo = makeAdditionalInfoByRoot(additionalInfos);
        if (!StringUtil.isEmptyOrSpaces(additionalInfo)) {
            Notifier.getInstance(myProject).notifyMinorInfo("Fetch details", additionalInfo);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean fetchAll = false;

        private Builder() {
        }

        public Builder fetchAll() {
            fetchAll = true;
            return this;
        }

        public GtFetcher build(@NotNull Project project, @NotNull ProgressIndicator progress) {
            return new GtFetcher(project, progress, this);
        }
    }
}
