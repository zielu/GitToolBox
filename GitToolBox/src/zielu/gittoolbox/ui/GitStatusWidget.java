package zielu.gittoolbox.ui;

import com.google.common.base.Optional;
import com.intellij.ide.ui.UISettings;
import com.intellij.ide.ui.UISettingsListener;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.EditorBasedWidget;
import com.intellij.util.Consumer;
import com.intellij.util.text.DateFormatUtil;
import com.intellij.util.ui.UIUtil;
import git4idea.GitVcs;
import git4idea.branch.GitBranchUtil;
import git4idea.repo.GitRepository;
import git4idea.util.GitUIUtil;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.GitToolBoxConfig;
import zielu.gittoolbox.GitToolBoxConfigForProject;
import zielu.gittoolbox.GitToolBoxConfigNotifier;
import zielu.gittoolbox.GitToolBoxProject;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.cache.PerRepoInfoCache;
import zielu.gittoolbox.cache.PerRepoStatusCacheListener;
import zielu.gittoolbox.cache.RepoInfo;
import zielu.gittoolbox.status.GitAheadBehindCount;
import zielu.gittoolbox.util.Html;

public class GitStatusWidget extends EditorBasedWidget implements StatusBarWidget.Multiframe, StatusBarWidget.TextPresentation {
    public static final String id = GitStatusWidget.class.getName();
    private final AtomicBoolean opened = new AtomicBoolean();
    private final AtomicReference<Optional<GitAheadBehindCount>> myCurrentAheadBehind = new AtomicReference<Optional<GitAheadBehindCount>>();
    private String myText = "";
    private boolean myVisible = true;

    private GitStatusWidget(@NotNull Project project) {
        super(project);
        myConnection.subscribe(PerRepoInfoCache.CACHE_CHANGE, new PerRepoStatusCacheListener() {
            @Override
            public void stateChanged(@NotNull final RepoInfo info, @NotNull final GitRepository repository) {
                UIUtil.invokeLaterIfNeeded(new Runnable() {
                    @Override
                    public void run() {
                        if (opened.get()) {
                            if (repository.equals(GitBranchUtil.getCurrentRepository(myProject))) {
                                update(repository, info.count);
                                updateStatusBar();
                            }
                        }
                    }
                });
            }
        });
        myConnection.subscribe(UISettingsListener.TOPIC, new UISettingsListener() {
            @Override
            public void uiSettingsChanged(UISettings uiSettings) {
                runUpdateLater();
            }
        });
        myConnection.subscribe(GitToolBoxConfigNotifier.CONFIG_TOPIC, new GitToolBoxConfigNotifier.Adapter() {
            @Override
            public void configChanged(GitToolBoxConfig config) {
                runUpdateLater();
            }
        });
    }

    private void runUpdateLater() {
        UIUtil.invokeLaterIfNeeded(new Runnable() {
            @Override
            public void run() {
                if (opened.get()) {
                    runUpdate();
                }
            }
        });
    }

    public static GitStatusWidget create(@NotNull Project project) {
        return new GitStatusWidget(project);
    }

    public void setVisible(boolean visible) {
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

    @NotNull
    @Override
    public String getText() {
        return myText;
    }

    @NotNull
    @Override
    public String getMaxPossibleText() {
        return "0000000000000000";
    }

    @Override
    public float getAlignment() {
        return Component.LEFT_ALIGNMENT;
    }

    @Nullable
    @Override
    public String getTooltipText() {
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
        myCurrentAheadBehind.set(null);
        myText = "";
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

    private void updateData(Optional<GitAheadBehindCount> aheadBehind) {
        myCurrentAheadBehind.set(aheadBehind);
        if (aheadBehind.isPresent()) {
            String statusText = StatusText.format(aheadBehind.get());
            myText = ResBundle.getString("status.prefix") + " " + statusText;
        } else {
            myText = ResBundle.getString("status.prefix") + " " + ResBundle.getString("git.na");
        }
    }

    private void runUpdate() {
        GitVcs git = GitVcs.getInstance(myProject);
        if (git != null) {
            GitRepository repository = GitBranchUtil.getCurrentRepository(myProject);
            Optional<GitAheadBehindCount> aheadBehind = Optional.absent();
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

    private void update(@Nullable GitRepository repository, Optional<GitAheadBehindCount> aheadBehind) {
        if (myVisible) {
            if (repository != null) {
                updateData(aheadBehind);
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
        return new Consumer<MouseEvent>() {
            @Override
            public void consume(MouseEvent mouseEvent) {
                runUpdate();
            }
        };
    }

    public void installed() {
        opened.compareAndSet(false, true);
    }

    public void uninstalled() {
        opened.compareAndSet(true, false);
    }
}
