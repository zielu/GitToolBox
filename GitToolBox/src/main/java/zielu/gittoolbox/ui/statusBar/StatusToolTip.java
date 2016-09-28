package zielu.gittoolbox.ui.statusBar;

import com.intellij.openapi.project.Project;
import com.intellij.util.containers.hash.LinkedHashMap;
import com.intellij.util.text.DateFormatUtil;
import git4idea.GitUtil;
import git4idea.repo.GitRepository;
import git4idea.util.GitUIUtil;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import jodd.util.StringBand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.GitToolBoxConfigForProject;
import zielu.gittoolbox.GitToolBoxProject;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.cache.PerRepoInfoCache;
import zielu.gittoolbox.cache.RepoInfo;
import zielu.gittoolbox.status.GitAheadBehindCount;
import zielu.gittoolbox.ui.StatusText;
import zielu.gittoolbox.util.GtUtil;
import zielu.gittoolbox.util.Html;

public class StatusToolTip {
    private final Project myProject;
    private GitRepository myCurrentRepository;

    private String myCurrentStatusText;

    public StatusToolTip(@NotNull Project project) {
        myProject = project;
    }

    @Nullable
    public String getText() {
        return prepateToolTip();
    }

    private String prepateToolTip() {
        StringBand infoPart = prepareInfoToolTipPart();
        if (infoPart.length() > 0) {
            infoPart.append(Html.br);
        }
        if (myCurrentStatusText == null) {
            myCurrentStatusText = prepareStatusTooltip();
        }
        infoPart.append(myCurrentStatusText);
        return infoPart.toString();
    }

    private String prepareStatusTooltip() {
        StringBand infoPart = new StringBand();
        Collection<GitRepository> repositories = GitUtil.getRepositories(myProject);
        if (repositories.size() == 1) {
            PerRepoInfoCache cache = GitToolBoxProject.getInstance(myProject).perRepoStatusCache();
            RepoInfo info = cache.getInfo(myCurrentRepository);
            if (info.count != null) {
                infoPart.append(StatusText.formatToolTip(info.count));
            }
        } else if (repositories.size() > 2) {
            prepareMultiRepoTooltip(infoPart, repositories);
        }
        return infoPart.toString();
    }


    private void prepareMultiRepoTooltip(StringBand infoPart, Collection<GitRepository> repositories) {
        PerRepoInfoCache cache = GitToolBoxProject.getInstance(myProject).perRepoStatusCache();
        Map<GitRepository, String> statuses = new LinkedHashMap<>();
        final AtomicReference<GitRepository> currentRepo = new AtomicReference<>();
        for (GitRepository repository : GtUtil.sort(repositories)) {
            GitAheadBehindCount count = cache.getInfo(repository).count;
            if (count != null) {
                String statusText = StatusText.format(count);
                if (repository.equals(myCurrentRepository)) {
                    currentRepo.set(repository);
                }
                statuses.put(repository, statusText);
            }
        }
        if (!statuses.isEmpty()) {
            if (infoPart.length() > 0) {
                infoPart.append(Html.hr);
            }
            infoPart.append(
                statuses.entrySet().stream().map(e -> {
                    String repoStatus = GitUIUtil.bold(GtUtil.name(e.getKey())) + ": " + e.getValue();
                    if (Objects.equals(e.getKey(), currentRepo.get())) {
                        repoStatus = Html.u(repoStatus);
                    }
                    return repoStatus;
                }).collect(Collectors.joining(Html.br))
            );
        }
    }

    private StringBand prepareInfoToolTipPart() {
        GitToolBoxConfigForProject config = GitToolBoxConfigForProject.getInstance(myProject);
        StringBand result = new StringBand();
        if (config.autoFetch) {
            result.append(GitUIUtil.bold(ResBundle.getString("message.autoFetch"))).append(": ");
            long lastAutoFetch = GitToolBoxProject.getInstance(myProject).autoFetch().lastAutoFetch();
            if (lastAutoFetch != 0) {
                result.append(DateFormatUtil.formatBetweenDates(lastAutoFetch, System.currentTimeMillis()));
            } else {
                result.append(ResBundle.getString("common.on"));
            }
        }

        return result;
    }


    public void update(@NotNull GitRepository repository, @Nullable GitAheadBehindCount aheadBehind) {
        myCurrentRepository = repository;
        myCurrentStatusText = null;
    }

    public void clear() {
        myCurrentRepository = null;
        myCurrentStatusText = null;
    }
}
