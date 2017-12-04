package zielu.gittoolbox.fetch;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import git4idea.repo.GitRepository;
import zielu.gittoolbox.GitToolBoxProject;
import zielu.gittoolbox.cache.PerRepoInfoCache;
import zielu.gittoolbox.cache.RepoInfo;
import zielu.gittoolbox.util.GtUtil;

import java.util.List;

public enum AutoFetchStrategy {
    RepoWithRemotes("repoWithRemotes") {
        private final Logger LOG = Logger.getInstance(getClass());

        @Override
        public List<GitRepository> fetchableRepositories(List<GitRepository> repositories, Project project) {
            List<GitRepository> fetchable = Lists.newArrayListWithCapacity(repositories.size());
            for (GitRepository repository : repositories) {
                if (GtUtil.hasRemotes(repository)) {
                    fetchable.add(repository);
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Skip repo " + GtUtil.name(repository) + " - no remotes");
                    }
                }
            }
            return fetchable;
        }
    },
    CurrentBranchWithRemote("currentBranchWithRemote") {
        private final Logger LOG = Logger.getInstance(getClass());

        @Override
        public List<GitRepository> fetchableRepositories(List<GitRepository> repositories, Project project) {
            PerRepoInfoCache cache = GitToolBoxProject.getInstance(project).perRepoStatusCache();
            List<GitRepository> fetchable = Lists.newArrayListWithCapacity(repositories.size());
            for (GitRepository repository : repositories) {
                RepoInfo info = cache.getInfo(repository);
                if (info.status().hasRemoteBranch()) {
                    fetchable.add(repository);
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Skip repo " + GtUtil.name(repository) + " - no remote branch");
                    }
                }
            }
            return fetchable;
        }
    };

    private static final ImmutableMap<String, AutoFetchStrategy> strategies;

    static {
        ImmutableMap.Builder<String, AutoFetchStrategy> builder = ImmutableMap.builder();
        for (AutoFetchStrategy strategy : AutoFetchStrategy.values()) {
            builder.put(strategy.key(), strategy);
        }
        strategies = builder.build();
    }


    private final String myKey;

    AutoFetchStrategy(String key) {
        myKey = key;
    }

    public String key() {
        return myKey;
    }

    public static AutoFetchStrategy forKey(String key) {
        return strategies.get(key);
    }

    public abstract List<GitRepository> fetchableRepositories(List<GitRepository> repositories, Project project);

}
