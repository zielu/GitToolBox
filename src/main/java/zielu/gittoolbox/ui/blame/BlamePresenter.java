package zielu.gittoolbox.ui.blame;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.revision.RevisionInfo;
import zielu.gittoolbox.util.AppUtil;

public interface BlamePresenter {
  static BlamePresenter getInstance() {
    return AppUtil.getServiceInstance(BlamePresenter.class);
  }

  @NotNull
  String getEditorInline(@NotNull RevisionInfo revisionInfo);

  @NotNull
  String getStatusBar(@NotNull RevisionInfo revisionInfo);

  @NotNull
  String getPopup(@NotNull RevisionInfo revisionInfo, @Nullable String details);
}
