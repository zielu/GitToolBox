package zielu.gittoolbox.ui.statusbar;

import com.intellij.ide.ui.UISettingsListener;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.util.Conditions;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.EditorBasedWidget;
import com.intellij.util.Consumer;
import git4idea.repo.GitRepository;
import java.awt.event.MouseEvent;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.cache.PerRepoInfoCache;
import zielu.gittoolbox.cache.PerRepoStatusCacheListener;
import zielu.gittoolbox.cache.RepoInfo;
import zielu.gittoolbox.config.ConfigNotifier;
import zielu.gittoolbox.config.GitToolBoxConfig2;
import zielu.gittoolbox.status.GitAheadBehindCount;
import zielu.gittoolbox.ui.StatusText;
import zielu.gittoolbox.ui.util.AppUiUtil;
import zielu.gittoolbox.util.DisposeSafeRunnable;
import zielu.gittoolbox.util.GtUtil;

public class GitStatusWidget extends EditorBasedWidget implements StatusBarUi,
    StatusBarWidget.Multiframe, StatusBarWidget.MultipleTextValuesPresentation {

  private static final String ID = GitStatusWidget.class.getName();
  private final AtomicBoolean connected = new AtomicBoolean();
  private final StatusToolTip toolTip;
  private final RootActions rootActions;
  private String text = "";
  private boolean visible = true;

  private GitStatusWidget(@NotNull Project project) {
    super(project);
    toolTip = new StatusToolTip(project);
    rootActions = new RootActions(project);
  }

  public static GitStatusWidget create(@NotNull Project project) {
    return new GitStatusWidget(project);
  }

  private void onCacheChange(@NotNull Project project, @NotNull final RepoInfo info,
                             @NotNull final GitRepository repository) {
    Runnable onCacheChange = new DisposeSafeRunnable(project, () -> {
      if (isActive() && repository.equals(GtUtil.getCurrentRepositoryQuick(project))) {
        update(repository, info);
        updateStatusBar();
      }
    });
    AppUiUtil.invokeLaterIfNeeded(project, onCacheChange);
  }

  private void runUpdateLater(@NotNull Project project) {
    Runnable update = () -> {
      if (isActive()) {
        runUpdate(project);
      }
    };
    AppUiUtil.invokeLaterIfNeeded(project, update);
  }

  @Override
  public void setVisible(boolean visible) {
    this.visible = visible;
    if (visible) {
      if (connected.compareAndSet(false, true)) {
        connect();
      }
    }
    runUpdateLater(myProject);
  }

  private void connect() {
    myConnection.subscribe(PerRepoInfoCache.CACHE_CHANGE, new PerRepoStatusCacheListener() {
      @Override
      public void stateChanged(@NotNull RepoInfo info, @NotNull GitRepository repository) {
        if (isActive()) {
          onCacheChange(myProject, info, repository);
        }
      }
    });
    myConnection.subscribe(ConfigNotifier.CONFIG_TOPIC, new ConfigNotifier() {
      @Override
      public void configChanged(GitToolBoxConfig2 previous, GitToolBoxConfig2 current) {
        if (isActive()) {
          runUpdateLater(myProject);
        }
      }
    });
    myConnection.subscribe(UISettingsListener.TOPIC, uiSettings -> {
      if (isActive()) {
        AppUiUtil.invokeLaterIfNeeded(myProject, this::updateStatusBar);
      }
    });
  }

  @Override
  public StatusBarWidget copy() {
    return new GitStatusWidget(myProject);
  }

  @NotNull
  @Override
  public String ID() {
    return ID;
  }

  @Nullable
  @Override
  public WidgetPresentation getPresentation(@NotNull PlatformType platformType) {
    return this;
  }

  @Nullable
  @Override
  public ListPopup getPopupStep() {
    if (rootActions.update()) {
      String title = ResBundle.message("statusBar.status.menu.title");
      return new StatusActionGroupPopup(title, rootActions, myProject, Conditions.alwaysTrue());
    } else {
      return null;
    }
  }

  @Nullable
  @Override
  public String getSelectedValue() {
    return text;
  }

  @Nullable
  @Override
  public String getTooltipText() {
    return toolTip.getText();
  }


  @Override
  public void closed() {
    dispose();
  }

  @Override
  public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
    runUpdate(myProject);
  }

  @Override
  public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
    runUpdate(myProject);
  }

  @Override
  public void selectionChanged(@NotNull FileEditorManagerEvent event) {
    runUpdate(myProject);
  }

  private void updateStatusBar() {
    if (myStatusBar != null) {
      myStatusBar.updateWidget(ID());
    }
  }

  private void empty() {
    toolTip.clear();
    text = "";
  }

  private void disabled() {
    toolTip.clear();
    text = ResBundle.message("status.prefix") + " " + ResBundle.disabled();
  }

  private void updateData(@NotNull GitRepository repository, RepoInfo repoInfo) {
    GitAheadBehindCount count = repoInfo.count();
    if (count == null) {
      text = ResBundle.na();
    } else {
      text = StatusText.format(count);
    }
    toolTip.update(repository, count);
  }

  private void runUpdate(@Nullable Project project) {
    Optional.ofNullable(project).ifPresent(this::performUpdate);
  }

  private void performUpdate(@NotNull Project project) {
    GitRepository repository = GtUtil.getCurrentRepositoryQuick(project);
    RepoInfo repoInfo = RepoInfo.empty();
    if (repository != null) {
      repoInfo = PerRepoInfoCache.getInstance(project).getInfo(repository);
    }
    update(repository, repoInfo);
    updateStatusBar();
  }

  private void update(@Nullable GitRepository repository, RepoInfo repoInfo) {
    if (isActive()) {
      if (repository != null) {
        updateData(repository, repoInfo);
      } else {
        empty();
      }
    } else {
      disabled();
    }
  }

  private boolean isActive() {
    return !isDisposed() && visible;
  }

  @Nullable
  @Override
  public Consumer<MouseEvent> getClickConsumer() {
    return null;
  }
}
