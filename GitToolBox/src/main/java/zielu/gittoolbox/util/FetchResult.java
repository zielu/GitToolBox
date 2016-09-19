package zielu.gittoolbox.util;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import git4idea.update.GitFetchResult;

public class FetchResult {
    private final GitFetchResult myResult;
    private final ImmutableCollection<Exception> myErrors;

    public FetchResult(GitFetchResult result, Iterable<Exception> errors) {
        myResult = result;
        myErrors = ImmutableList.copyOf(errors);
    }

    public GitFetchResult result() {
        return myResult;
    }
}
