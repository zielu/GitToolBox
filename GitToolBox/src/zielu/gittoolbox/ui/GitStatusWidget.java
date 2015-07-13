package zielu.gittoolbox.ui;

import com.google.common.base.Optional;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.EditorBasedWidget;
import com.intellij.util.Consumer;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.UIUtil;
import git4idea.GitVcs;
import git4idea.branch.GitBranchUtil;
import git4idea.repo.GitRepository;
import java.awt.Component;
import java.awt.event.MouseEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.GitToolBoxProject;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.cache.PerRepoStatusCache;
import zielu.gittoolbox.cache.PerRepoStatusCacheListener;
import zielu.gittoolbox.status.GitAheadBehindCount;
import zielu.gittoolbox.status.Status;
import zielu.gittoolbox.status.StatusMessages;

public class GitStatusWidget extends EditorBasedWidget implements StatusBarWidget.Multiframe, StatusBarWidget.TextPresentation {
    private String myText = "";
    private String myToolTipText = "";
    private MessageBusConnection myCacheConnection;

    private GitStatusWidget(@NotNull Project project) {
        super(project);
        myCacheConnection = myProject.getMessageBus().connect(this);
        myCacheConnection.subscribe(PerRepoStatusCache.CACHE_CHANGE, new PerRepoStatusCacheListener() {
            @Override
            public void stateChanged(@NotNull final Optional<GitAheadBehindCount> aheadBehind, @NotNull final GitRepository repository) {
                UIUtil.invokeLaterIfNeeded(new Runnable() {
                    @Override
                    public void run() {
                        if (repository.equals(GitBranchUtil.getCurrentRepository(myProject))) {
                            update(repository, aheadBehind);
                        }
                    }
                });
            }
        });
    }

    @Override
    public void dispose() {
        myCacheConnection.disconnect();
        myCacheConnection = null;
        super.dispose();
    }

    public static StatusBarWidget create(@NotNull Project project) {
        return new GitStatusWidget(project);
    }

    @Override
    public StatusBarWidget copy() {
        return new GitStatusWidget(myProject);
    }

    @NotNull
    @Override
    public String ID() {
        return getClass().getName();
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
        return myToolTipText;
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
        myText = ResBundle.getString("status.prefix") + " " + ResBundle.getString("git.na");
        myToolTipText = "";
    }

    private void updateData(GitAheadBehindCount aheadBehind) {
        String statusText = StatusMessages.aheadBehindStatus(aheadBehind);
        if (aheadBehind.status() == Status.Success) {
            myText = ResBundle.getString("status.prefix") + " " + statusText;
            myToolTipText = "";
        } else {
            myText = ResBundle.getString("status.prefix") + " " + ResBundle.getString("git.na");
            myToolTipText = statusText;
        }
    }

    private void runUpdate() {
        GitVcs git = GitVcs.getInstance(myProject);
        if (git != null) {
            GitRepository repository = GitBranchUtil.getCurrentRepository(myProject);
            Optional<GitAheadBehindCount> aheadBehind = Optional.absent();
            if (repository != null) {
                GitToolBoxProject toolBox = GitToolBoxProject.getInstance(myProject);
                aheadBehind = toolBox.perRepoStatusCache().get(repository);

            }
            update(repository, aheadBehind);
        }
    }

    private void update(@Nullable GitRepository repository, Optional<GitAheadBehindCount> aheadBehind) {
        if (repository != null) {
            if (aheadBehind.isPresent()) {
                updateData(aheadBehind.get());
            } else {
                empty();
            }
        } else {
            empty();
        }
        updateStatusBar();
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
}
