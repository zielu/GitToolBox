package zielu.gittoolbox.ui.statusbar;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import javax.swing.Icon;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.GitToolBox;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.ResIcons;
import zielu.gittoolbox.cache.PerRepoInfoCache;
import zielu.gittoolbox.cache.PerRepoStatusCacheListener;
import zielu.gittoolbox.cache.RepoInfo;
import zielu.gittoolbox.cache.VirtualFileRepoCache;
import zielu.gittoolbox.changes.ChangesTrackerService;
import zielu.gittoolbox.config.AppConfig;
import zielu.gittoolbox.config.AppConfigNotifier;
import zielu.gittoolbox.config.GitToolBoxConfig2;
import zielu.gittoolbox.status.GitAheadBehindCount;
import zielu.gittoolbox.ui.ExtendedRepoInfo;
import zielu.gittoolbox.ui.ExtendedRepoInfoService;
import zielu.gittoolbox.ui.StatusText;
import zielu.gittoolbox.ui.util.AppUiUtil;
import zielu.gittoolbox.util.DisposeSafeRunnable;
import zielu.gittoolbox.util.GtUtil;

public class GitStatusWidget extends EditorBasedWidget implements StatusBarUi,
    StatusBarWidget.Multiframe, StatusBarWidget.MultipleTextValuesPresentation {

  public static final String ID = GitToolBox.PLUGIN_ID + "." + GitStatusWidget.class.getName();
  private final AtomicBoolean connected = new AtomicBoolean();
  private final AtomicBoolean visible = new AtomicBoolean();
  private final StatusToolTip toolTip;
  private final RootActions rootActions;
  private String text = "";
  private Icon icon = null;

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
    myConnection.subscribe(PerRepoInfoCache.CACHE_CHANGE, new PerRepoStatusCacheListener() {
      @Override
      public void stateChanged(@NotNull RepoInfo info, @NotNull GitRepository repository) {
        if (isActive()) {
          onCacheChange(myProject, info, repository);
        }
      }
    });
    myConnection.subscribe(AppConfigNotifier.CONFIG_TOPIC, new AppConfigNotifier() {
      @Override
      public void configChanged(@NotNull GitToolBoxConfig2 previous, @NotNull GitToolBoxConfig2 current) {
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
    AppUiUtil.invokeLaterIfNeeded(myProject, this::updateStatusBar);
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
    if (visible.get() && rootActions.update()) {
      String title = ResBundle.message("statusBar.status.menu.title");
      return new StatusActionGroupPopup(title, rootActions, myProject, Conditions.alwaysTrue());
    } else {
      return null;
    }
  }

  @Nullable
  @Override
  public String getSelectedValue() {
    if (visible.get()) {
      return text;
    } else {
      return null;
    }
  }

  @Nullable
  @Override
  public String getTooltipText() {
    if (visible.get()) {
      return toolTip.getText();
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
    setVisible(isVisibleFromConfig());
  }

  private boolean isVisibleFromConfig() {
    return AppConfig.get().getShowStatusWidget();
  }

  @Override
  public void dispose() {
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

  private void empty() {
    toolTip.clear();
    text = "";
    icon = null;
  }

  private void disabled() {
    toolTip.clear();
    text = ResBundle.message("status.prefix") + " " + ResBundle.disabled();
    icon = null;
  }

  private void updateData(@NotNull GitRepository repository, RepoInfo repoInfo, ExtendedRepoInfo extendedInfo) {
    icon = null;
    List<String> parts = new ArrayList<>();
    GitToolBoxConfig2 config = AppConfig.get();
    if (config.getShowChangesInStatusBar()) {
      parts.add(StatusText.format(extendedInfo));
      if (extendedInfo.hasChanged()) {
        if (extendedInfo.getChangedCount().getValue() > 0) {
          icon = ResIcons.ChangesPresent;
        } else {
          icon = ResIcons.NoChanges;
        }
      }
    }
    toolTip.update(repository, null);
    if (config.getShowStatusWidget()) {
      GitAheadBehindCount count = repoInfo.count();
      if (count == null) {
        parts.add(ResBundle.na());
      } else {
        parts.add(StatusText.format(count));
      }
      toolTip.update(repository, count);
    }
    text = parts.stream()
               .filter(StringUtils::isNotBlank)
               .collect(Collectors.joining(" / "));
  }

  private void runUpdate(@Nullable Project project, @NotNull VirtualFile file) {
    Optional.ofNullable(project).ifPresent(prj -> performUpdate(prj, file));
  }

  private void runUpdate(@Nullable Project project) {
    Optional.ofNullable(project).ifPresent(this::performUpdate);
  }

  private void performUpdate(@NotNull Project project, @NotNull VirtualFile file) {
    VirtualFileRepoCache fileCache = VirtualFileRepoCache.getInstance(project);
    GitRepository repo = fileCache.getRepoForFile(file);
    performUpdate(project, repo);
  }

  private void performUpdate(@NotNull Project project) {
    GitRepository repo = GtUtil.getCurrentRepositoryQuick(project);
    performUpdate(project, repo);
  }

  private void performUpdate(@NotNull Project project, @Nullable GitRepository repository) {
    RepoInfo repoInfo = RepoInfo.empty();
    if (repository != null) {
      repoInfo = PerRepoInfoCache.getInstance(project).getInfo(repository);
    }
    update(repository, repoInfo);
    repaintStatusBar();
  }

  private void update(@Nullable GitRepository repository, RepoInfo repoInfo) {
    if (isActive()) {
      if (repository != null) {
        ExtendedRepoInfo extendedInfo = ExtendedRepoInfoService.getInstance().getExtendedRepoInfo(repository);
        updateData(repository, repoInfo, extendedInfo);
      } else {
        empty();
      }
    } else {
      disabled();
    }
  }

  private boolean isActive() {
    return !isDisposed() && visible.get();
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
      return icon;
    } else {
      return null;
    }
  }
}
