package zielu.gittoolbox.completion;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import java.io.File;
import java.util.Collection;
import java.util.List;
import zielu.gittoolbox.config.GitToolBoxConfigForProject;
import zielu.gittoolbox.util.LogWatch;

public class CompletionCheckinHandler extends CheckinHandler {
    private final Logger LOG = Logger.getInstance(getClass());
    private final CheckinProjectPanel myPanel;

    public CompletionCheckinHandler(CheckinProjectPanel panel) {
        this.myPanel = panel;
        captureSelectedRepositories(myPanel);
    }

    @Override
    public void includedChangesChanged() {
        captureSelectedRepositories(myPanel);
    }

    private void captureSelectedRepositories(CheckinProjectPanel panel) {
        GitToolBoxConfigForProject config = GitToolBoxConfigForProject.getInstance(panel.getProject());
        if (config.commitDialogCompletion) {
            LogWatch getAffectedWatch = LogWatch.createStarted("Get affected");
            Collection<File> affected = panel.getFiles();
            getAffectedWatch.finish();
            GitToolBoxCompletionProject.getInstance(panel.getProject()).updateAffected(affected);
        }
    }

    @Override
    public void checkinSuccessful() {
        dispose();
    }

    @Override
    public void checkinFailed(List<VcsException> exception) {
        dispose();
    }

    private void dispose() {
        GitToolBoxCompletionProject.getInstance(myPanel.getProject()).clearAffected();
    }
}
