package zielu.gittoolbox.ui.blame;

import com.intellij.openapi.components.ServiceManager;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.revision.RevisionInfo;

public interface BlamePresenter {
  static BlamePresenter getInstance() {
    return ServiceManager.getService(BlamePresenter.class);
  }

  @NotNull
  String getEditorInline(@NotNull RevisionInfo revisionInfo);

  @NotNull
  String getStatusBar(@NotNull RevisionInfo revisionInfo);

  @NotNull
  String getPopup(@NotNull RevisionInfo revisionInfo);
}
