package zielu.gittoolbox.ui.statusbar;

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
import git4idea.repo.GitRepository;
import java.awt.event.MouseEvent;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.GitToolBoxProject;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.cache.PerRepoInfoCacheImpl;
import zielu.gittoolbox.cache.PerRepoStatusCacheListener;
import zielu.gittoolbox.cache.RepoInfo;
import zielu.gittoolbox.config.ConfigNotifier;
import zielu.gittoolbox.config.GitToolBoxConfig;
import zielu.gittoolbox.ui.StatusText;
import zielu.gittoolbox.ui.util.AppUtil;
import zielu.gittoolbox.util.GtUtil;

public class GitStatusWidget extends EditorBasedWidget implements StatusBarWidget.Multiframe,
    StatusBarWidget.MultipleTextValuesPresentation {
  private static final String ID = GitStatusWidget.class.getName();
  private final AtomicBoolean opened = new AtomicBoolean();
  private final StatusToolTip toolTip;
  private final RootActions rootActions;
  private String text = "";
  private boolean visible = true;


  private GitStatusWidget(@NotNull Project project) {
    super(project);
    toolTip = new StatusToolTip(project);
    rootActions = new RootActions(project);
    myConnection.subscribe(PerRepoInfoCacheImpl.CACHE_CHANGE, new PerRepoStatusCacheListener() {
      @Override
      public void stateChanged(@NotNull RepoInfo info, @NotNull GitRepository repository) {
        onCacheChange(info, repository);
      }
    });
    myConnection.subscribe(ConfigNotifier.CONFIG_TOPIC, new ConfigNotifier.Adapter() {
      @Override
      public void configChanged(GitToolBoxConfig config) {
        runUpdateLater();
      }
    });
    myConnection.subscribe(UISettingsListener.TOPIC, uiSettings -> AppUtil.invokeLaterIfNeeded(this::updateStatusBar));
  }

  public static GitStatusWidget create(@NotNull Project project) {
    return new GitStatusWidget(project);
  }

  private void onCacheChange(@NotNull final RepoInfo info, @NotNull final GitRepository repository) {
    AppUtil.invokeLaterIfNeeded(() -> {
      if (opened.get() && repository.equals(GtUtil.getCurrentRepositoryQuick(myProject))) {
        update(repository, info);
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

  void setVisible(boolean visible) {
    this.visible = visible;
    runUpdateLater();
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
      String title = ResBundle.getString("statusBar.menu.title");
      return new StatusActionGroupPopup(title, rootActions, myProject, Condition.TRUE);
    } else {
      return null;
    }
  }

  @Nullable
  @Override
  public String getSelectedValue() {
    return text;
  }

  @NotNull
  @Override
  public String getMaxValue() {
    return "";
  }

  @Nullable
  @Override
  public String getTooltipText() {
    return toolTip.getText();
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
    toolTip.clear();
    text = "";
  }

  private void updateData(@NotNull GitRepository repository, RepoInfo repoInfo) {
    toolTip.update(repository, repoInfo.count().orElse(null));
    text = ResBundle.getString("status.prefix") + " " + repoInfo.count().map(StatusText::format)
        .orElse(ResBundle.getString("git.na"));
  }

  private void runUpdate() {
    GitVcs git = GitVcs.getInstance(myProject);
    if (git != null) {
      GitRepository repository = GtUtil.getCurrentRepositoryQuick(myProject);
      RepoInfo repoInfo = RepoInfo.empty();
      if (repository != null) {
        GitToolBoxProject toolBox = GitToolBoxProject.getInstance(myProject);
        repoInfo = toolBox.perRepoStatusCache().getInfo(repository);
      }
      update(repository, repoInfo);
    } else {
      empty();
    }
    updateStatusBar();
  }

  private void update(@Nullable GitRepository repository, RepoInfo repoInfo) {
    if (visible) {
      if (repository != null) {
        updateData(repository, repoInfo);
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
