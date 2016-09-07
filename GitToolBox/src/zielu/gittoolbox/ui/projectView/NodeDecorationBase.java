package zielu.gittoolbox.ui.projectView;

import com.google.common.base.Optional;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.GitToolBoxConfig;
import zielu.gittoolbox.status.GitAheadBehindCount;
import zielu.gittoolbox.status.Status;
import zielu.gittoolbox.ui.StatusPresenter;

public abstract class NodeDecorationBase implements NodeDecoration {
    protected final GitToolBoxConfig config;

    protected NodeDecorationBase(GitToolBoxConfig config) {
        this.config = config;
    }

    @Nullable
    protected final String getCountText(Optional<GitAheadBehindCount> aheadBehind) {
        if (aheadBehind.isPresent()) {
            GitAheadBehindCount count = aheadBehind.get();
            if (count.status() == Status.Success) {
                StatusPresenter presenter = config.getPresenter();
                String text = presenter.nonZeroAheadBehindStatus(count.ahead.value(), count.behind.value());
                if (StringUtils.isNotBlank(text)) {
                    return text;
                }
            }
        }
        return null;
    }
}
