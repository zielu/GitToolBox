package zielu.gittoolbox.ui.blame;

import com.intellij.openapi.components.ServiceManager;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.blame.Blame;

public interface BlamePresenter {
  static BlamePresenter getInstance() {
    return ServiceManager.getService(BlamePresenter.class);
  }

  @NotNull
  String getEditorInline(@NotNull Blame blame);

  @NotNull
  String getStatusBar(@NotNull Blame blame);

  @NotNull
  String getPopup(@NotNull Blame blame);
}
