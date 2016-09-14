package zielu.gittoolbox.ui.statusBar;

import com.intellij.openapi.project.Project;
import com.intellij.util.text.DateFormatUtil;
import git4idea.GitUtil;
import git4idea.repo.GitRepository;
import git4idea.util.GitUIUtil;
import java.util.Collection;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.GitToolBoxConfigForProject;
import zielu.gittoolbox.GitToolBoxProject;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.cache.PerRepoInfoCache;
import zielu.gittoolbox.status.GitAheadBehindCount;
import zielu.gittoolbox.ui.StatusText;
import zielu.gittoolbox.util.Html;

public class StatusToolTip {
    private GitRepository myCurrentRepository;
    private GitAheadBehindCount myCurrentAheadBehind;
    private final Project myProject;

    public StatusToolTip(@NotNull Project project) {
        myProject = project;
    }

    @Nullable
    public String getText() {
        if (myCurrentAheadBehind == null) {
            return prepareInfoToolTipPart();
        } else {
            String infoPart = prepareInfoToolTipPart();
            if (infoPart.length() > 0) {
                infoPart += Html.br;
            }
            Collection<GitRepository> repositories = GitUtil.getRepositories(myProject);
            if (repositories.size() == 1) {
                return infoPart + StatusText.formatToolTip(myCurrentAheadBehind);
            } else if (repositories.size() > 2) {
                PerRepoInfoCache cache = GitToolBoxProject.getInstance(myProject).perRepoStatusCache();
                TreeMap<String, String> statuses = new TreeMap<>();
                String currentRepoKey = null;
                for (GitRepository repository : repositories) {
                    GitAheadBehindCount count = cache.getInfo(repository).count;
                    if (count != null) {
                        String name = repository.getRoot().getName();
                        String statusText = StatusText.format(count);
                        if (repository.equals(myCurrentRepository)) {
                            currentRepoKey = name;
                        }
                        statuses.put(name, statusText);
                    }
                }
                if (!statuses.isEmpty()) {
                    StringBuilder finalBuilder = new StringBuilder(infoPart);
                    if (finalBuilder.length() > 0) {
                        finalBuilder.append(Html.hr);
                    }
                    final String currentName = currentRepoKey;
                    finalBuilder.append(
                        statuses.entrySet().stream().map(e -> {
                            String repoStatus = GitUIUtil.bold(e.getKey()) + ": " + e.getValue();
                            if (e.getKey().equals(currentName)) {
                                repoStatus = Html.u(repoStatus);
                            }
                            return repoStatus;
                        }).collect(Collectors.joining(Html.br))
                    );
                    return finalBuilder.toString();
                }
            }
            return infoPart;
        }
    }

    private String prepareInfoToolTipPart() {
        GitToolBoxConfigForProject config = GitToolBoxConfigForProject.getInstance(myProject);
        StringBuilder result = new StringBuilder();
        if (config.autoFetch) {
            result.append(GitUIUtil.bold(ResBundle.getString("message.autoFetch") + ": "));
            long lastAutoFetch = GitToolBoxProject.getInstance(myProject).autoFetch().lastAutoFetch();
            if (lastAutoFetch != 0) {
                result.append(DateFormatUtil.formatBetweenDates(lastAutoFetch, System.currentTimeMillis()));
            } else {
                result.append(ResBundle.getString("common.on"));
            }
        }

        return result.toString();
    }


    public void update(@NotNull GitRepository repository, @Nullable GitAheadBehindCount aheadBehind) {
        myCurrentRepository = repository;
        myCurrentAheadBehind = aheadBehind;
    }

    public void clear() {
        myCurrentRepository = null;
        myCurrentAheadBehind = null;
    }
}
