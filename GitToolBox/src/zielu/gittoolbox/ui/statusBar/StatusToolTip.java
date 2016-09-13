package zielu.gittoolbox.ui.statusBar;

import static com.intellij.testFramework.LightPlatformTestCase.*;

import com.google.common.base.Optional;
import com.intellij.openapi.project.Project;
import com.intellij.util.text.DateFormatUtil;
import git4idea.repo.GitRepository;
import git4idea.util.GitUIUtil;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.GitToolBoxConfigForProject;
import zielu.gittoolbox.GitToolBoxProject;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.status.GitAheadBehindCount;
import zielu.gittoolbox.ui.StatusText;
import zielu.gittoolbox.util.Html;

public class StatusToolTip {
    private final AtomicReference<Optional<GitAheadBehindCount>> myCurrentAheadBehind = new AtomicReference<Optional<GitAheadBehindCount>>();
    private final Project myProject;

    public StatusToolTip(@NotNull Project project) {
        myProject = project;
    }

    @Nullable
    public String getText() {
        Optional<GitAheadBehindCount> currentAheadBehind = myCurrentAheadBehind.get();
        if (currentAheadBehind == null) {
            return "";
        } else {
            Optional<GitAheadBehindCount> aheadBehind = currentAheadBehind;
            if (aheadBehind.isPresent()) {
                String infoPart = prepareInfoToolTipPart();
                if (infoPart.length() > 0) {
                    infoPart += Html.br;
                }
                return infoPart + StatusText.formatToolTip(aheadBehind.get());
            } else {
                return prepareInfoToolTipPart();
            }
        }
    }

    private String prepareInfoToolTipPart() {
        GitToolBoxConfigForProject config = GitToolBoxConfigForProject.getInstance(getProject());
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


    public void update(@NotNull GitRepository repository, @NotNull Optional<GitAheadBehindCount> aheadBehind) {
        myCurrentAheadBehind.set(aheadBehind);
    }

    public void clear() {
        myCurrentAheadBehind.set(null);
    }
}
