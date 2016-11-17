package zielu.gittoolbox.ui.statusBar;

import com.intellij.ide.ui.UISettingsListener;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.EditorBasedWidget;
import com.intellij.util.Consumer;
import git4idea.GitVcs;
import git4idea.branch.GitBranchUtil;
import git4idea.repo.GitRepository;
import java.awt.event.MouseEvent;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.ConfigNotifier;
import zielu.gittoolbox.GitToolBoxConfig;
import zielu.gittoolbox.GitToolBoxProject;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.cache.PerRepoInfoCache;
import zielu.gittoolbox.cache.PerRepoStatusCacheListener;
import zielu.gittoolbox.cache.RepoInfo;
import zielu.gittoolbox.status.GitAheadBehindCount;
import zielu.gittoolbox.ui.StatusText;
import zielu.gittoolbox.ui.util.AppUtil;

public class GitStatusWidget extends EditorBasedWidget implements StatusBarWidget.Multiframe,
            StatusBarWidget.MultipleTextValuesPresentation {
    public static final String id = GitStatusWidget.class.getName();
    private final AtomicBoolean opened = new AtomicBoolean();
    private final StatusToolTip myToolTip;
    private final RootActions myRootActions;
    private String myText = "";
    private boolean myVisible = true;


    private GitStatusWidget(@NotNull Project project) {
        super(project);
        myToolTip = new StatusToolTip(project);
        myRootActions = new RootActions(project);
        myConnection.subscribe(PerRepoInfoCache.CACHE_CHANGE, new PerRepoStatusCacheListener() {
            @Override
            public void stateChanged(@NotNull RepoInfo info, @NotNull GitRepository repository) {
                onCacheChange(info, repository);
            }
        });
        myConnection.subscribe(UISettingsListener.TOPIC, uiSettings -> runUpdateLater());
        myConnection.subscribe(ConfigNotifier.CONFIG_TOPIC, new ConfigNotifier.Adapter() {
            @Override
            public void configChanged(GitToolBoxConfig config) {
                runUpdateLater();
            }
        });
    }

    private void onCacheChange(@NotNull final RepoInfo info, @NotNull final GitRepository repository) {
        AppUtil.invokeLaterIfNeeded(() -> {
            if (opened.get() && repository.equals(GitBranchUtil.getCurrentRepository(myProject))) {
                update(repository, info.count);
                updateStatusBar();
            }
        });
    }

    private void runUpdateLater() {
        AppUtil.invokeLaterIfNeeded(() -> {
            if (opened.get()) {
                runUpdate();
            }
        });
    }

    public static GitStatusWidget create(@NotNull Project project) {
        return new GitStatusWidget(project);
    }

    void setVisible(boolean visible) {
        myVisible = visible;
        runUpdateLater();
    }

    @Override
    public StatusBarWidget copy() {
        return new GitStatusWidget(myProject);
    }

    @NotNull
    @Override
    public String ID() {
        return id;
    }

    @Nullable
    @Override
    public WidgetPresentation getPresentation(@NotNull PlatformType platformType) {
        return this;
    }

    @Nullable
    @Override
    public ListPopup getPopupStep() {
        if (myRootActions.update()) {
            return new StatusActionGroupPopup(ResBundle.getString("statusBar.menu.title"), myRootActions, myProject, Condition.TRUE);
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public String getSelectedValue() {
        return myText;
    }

    @NotNull
    @Override
    public String getMaxValue() {
        return "";
    }

    @Nullable
    @Override
    public String getTooltipText() {
        return myToolTip.getText();
    }

    @Override
    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        runUpdate();
    }

    @Override
    public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        runUpdate();
    }

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        runUpdate();
    }

    private void updateStatusBar() {
        if (myStatusBar != null) {
            myStatusBar.updateWidget(ID());
        }
    }

    private void empty() {
        myToolTip.clear();
        myText = "";
    }

    private void updateData(@NotNull GitRepository repository, @Nullable GitAheadBehindCount aheadBehind) {
        myToolTip.update(repository, aheadBehind);
        if (aheadBehind != null) {
            String statusText = StatusText.format(aheadBehind);
            myText = ResBundle.getString("status.prefix") + " " + statusText;
        } else {
            myText = ResBundle.getString("status.prefix") + " " + ResBundle.getString("git.na");
        }
    }

    private void runUpdate() {
        GitVcs git = GitVcs.getInstance(myProject);
        if (git != null) {
            GitRepository repository = GitBranchUtil.getCurrentRepository(myProject);
            GitAheadBehindCount aheadBehind = null;
            if (repository != null) {
                GitToolBoxProject toolBox = GitToolBoxProject.getInstance(myProject);
                aheadBehind = toolBox.perRepoStatusCache().getInfo(repository).count;
            }
            update(repository, aheadBehind);
        } else {
            empty();
        }
        updateStatusBar();
    }

    private void update(@Nullable GitRepository repository, @Nullable GitAheadBehindCount aheadBehind) {
        if (myVisible) {
            if (repository != null) {
                updateData(repository, aheadBehind);
            } else {
                empty();
            }
        } else {
            empty();
        }
    }

    @Nullable
    @Override
    public Consumer<MouseEvent> getClickConsumer() {
        return null;
    }

    public void installed() {
        opened.compareAndSet(false, true);
    }

    public void uninstalled() {
        opened.compareAndSet(true, false);
    }
}
