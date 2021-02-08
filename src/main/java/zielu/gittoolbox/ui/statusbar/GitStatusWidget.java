package zielu.gittoolbox.ui.statusbar;

import static zielu.gittoolbox.cache.PerRepoInfoCache.CACHE_CHANGE_TOPIC;

import com.intellij.ide.ui.UISettingsListener;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.util.Conditions;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.EditorBasedWidget;
import com.intellij.util.Consumer;
import git4idea.repo.GitRepository;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.GitToolBox;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.cache.PerRepoStatusCacheListener;
import zielu.gittoolbox.cache.RepoInfo;
import zielu.gittoolbox.changes.ChangesTrackerService;
import zielu.gittoolbox.config.AppConfigNotifier;
import zielu.gittoolbox.ui.ExtendedRepoInfo;
import zielu.gittoolbox.ui.util.AppUiUtil;

public class GitStatusWidget extends EditorBasedWidget implements StatusBarUi,
    StatusBarWidget.Multiframe, StatusBarWidget.MultipleTextValuesPresentation {

  public static final String ID = GitToolBox.PLUGIN_ID + "." + GitStatusWidget.class.getName();
  private final AtomicBoolean connected = new AtomicBoolean();
  private final AtomicBoolean visible = new AtomicBoolean();
  private final AtomicBoolean active = new AtomicBoolean(true);
  private final GitStatusWidgetFacade facade = new GitStatusWidgetFacade();
  private final GitStatusPresenter presenter;

  private GitStatusWidget(@NotNull Project project) {
    super(project);
    presenter = new GitStatusPresenter(project);
  }

  public static GitStatusWidget create(@NotNull Project project) {
    return new GitStatusWidget(project);
  }

  private void onCacheChange(@NotNull Project project, @NotNull final RepoInfo info,
                             @NotNull final GitRepository repository) {
    Runnable onCacheChange = () -> {
      if (isActive()) {
        VirtualFile selectedFile = getSelectedFile();
        if (selectedFile != null) {
          performUpdate(project, selectedFile);
        } else {
          performUpdate(project);
        }
      }
    };
    if (isActive()) {
      AppUiUtil.invokeLaterIfNeeded(this, onCacheChange);
    }
  }

  private void runUpdateLater(@NotNull Project project) {
    Runnable update = () -> {
      if (isActive()) {
        runUpdate(project);
      }
    };
    if (isActive()) {
      AppUiUtil.invokeLaterIfNeeded(this, update);
    }
  }

  private void setVisible(boolean visible) {
    this.visible.set(visible);
    runUpdateLater(myProject);
  }

  private void initialize() {
    if (connected.compareAndSet(false, true)) {
      connect();
    }
  }

  private void connect() {
    myConnection.subscribe(CACHE_CHANGE_TOPIC, new PerRepoStatusCacheListener() {
      @Override
      public void stateChanged(@NotNull RepoInfo info, @NotNull GitRepository repository) {
        if (isActive()) {
          onCacheChange(myProject, info, repository);
        }
      }
    });
    myConnection.subscribe(AppConfigNotifier.CONFIG_TOPIC, (previous, current) -> {
      if (isActive()) {
        updateVisibleFromConfig();
        if (current.getShowStatusWidget()) {
          runUpdateLater(myProject);
        }
        if (current.getShowStatusWidget() != previous.getShowStatusWidget()) {
          repaintStatusBar();
        }
      }
    });
    myConnection.subscribe(UISettingsListener.TOPIC, uiSettings -> {
      if (isActive()) {
        repaintStatusBar();
      }
    });
    myConnection.subscribe(ChangesTrackerService.CHANGES_TRACKER_TOPIC, () -> {
      if (isActive()) {
        runUpdateLater(myProject);
      }
    });
  }

  private void repaintStatusBar() {
    AppUiUtil.invokeLaterIfNeeded(this, this::updateStatusBar);
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
  public WidgetPresentation getPresentation() {
    return this;
  }

  @Nullable
  @Override
  public ListPopup getPopupStep() {
    if (visible.get()) {
      String title = ResBundle.message("statusBar.status.menu.title");
      return new StatusActionGroupPopup(title, myProject, Conditions.alwaysTrue());
    } else {
      return null;
    }
  }

  @Nullable
  @Override
  public String getSelectedValue() {
    if (visible.get()) {
      return presenter.getText();
    } else {
      return null;
    }
  }

  @Nullable
  @Override
  public String getTooltipText() {
    if (visible.get()) {
      return presenter.getToolTipText();
    } else {
      return null;
    }
  }

  @Override
  public void install(@NotNull StatusBar statusBar) {
    super.install(statusBar);
    initialize();
    updateVisibleFromConfig();
  }

  private void updateVisibleFromConfig() {
    setVisible(facade.getIsVisibleConfig());
  }

  @Override
  public void dispose() {
    active.compareAndSet(true, false);
    setVisible(false);
    super.dispose();
  }

  @Override
  public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
    runUpdate(myProject, file);
  }

  @Override
  public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
    runUpdate(myProject);
  }

  @Override
  public void selectionChanged(@NotNull FileEditorManagerEvent event) {
    VirtualFile file = event.getNewFile();
    if (file != null) {
      runUpdate(myProject, file);
    } else {
      runUpdate(myProject);
    }
  }

  private void updateStatusBar() {
    if (myStatusBar != null) {
      myStatusBar.updateWidget(ID());
    }
  }

  private void runUpdate(@Nullable Project project, @NotNull VirtualFile file) {
    Optional.ofNullable(project).ifPresent(prj -> performUpdate(prj, file));
  }

  private void runUpdate(@Nullable Project project) {
    Optional.ofNullable(project).ifPresent(this::performUpdate);
  }

  private void performUpdate(@NotNull Project project, @NotNull VirtualFile file) {
    performRepoUpdate(project, facade.getRepoForFile(project, file));
  }

  private void performUpdate(@NotNull Project project) {
    performRepoUpdate(project, null);
  }

  private void performRepoUpdate(@NotNull Project project, @Nullable GitRepository repository) {
    if (isActive()) {
      RepoInfo repoInfo = RepoInfo.empty();
      if (repository != null) {
        repoInfo = facade.getRepoInfo(repository);
      }
      if (repository != null) {
        updateForRepo(repository, repoInfo);
      } else {
        updateForNoRepo(project);
      }
    } else {
      presenter.disabled();
    }
    repaintStatusBar();
  }

  private void updateForRepo(@NotNull GitRepository repository, @NotNull RepoInfo repoInfo) {
    ExtendedRepoInfo extendedInfo = facade.getExtendedRepoInfo(repository);
    presenter.updateData(repository, repoInfo, extendedInfo);
  }

  private void updateForNoRepo(@NotNull Project project) {
    List<RepoInfo> repoInfos = facade.getRepoInfos(project);
    ExtendedRepoInfo extendedInfo = facade.getExtendedRepoInfo(project);
    presenter.updateData(repoInfos, extendedInfo);
  }

  private boolean isActive() {
    return active.get() && visible.get();
  }

  @Nullable
  @Override
  public Consumer<MouseEvent> getClickConsumer() {
    return null;
  }

  @Nullable
  @Override
  public Icon getIcon() {
    if (visible.get()) {
      return presenter.getIcon();
    } else {
      return null;
    }
  }
}
