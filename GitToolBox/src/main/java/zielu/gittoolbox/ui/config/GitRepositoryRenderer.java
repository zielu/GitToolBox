package zielu.gittoolbox.ui.config;

import static com.intellij.ui.SimpleTextAttributes.ERROR_ATTRIBUTES;
import static com.intellij.ui.SimpleTextAttributes.GRAYED_ATTRIBUTES;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.ui.ColoredListCellRenderer;
import git4idea.repo.GitRepository;
import java.util.Optional;
import javax.swing.JList;
import jodd.util.StringBand;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.util.GtUtil;

public class GitRepositoryRenderer extends ColoredListCellRenderer<String> {
  private final Project project;

  public GitRepositoryRenderer(@NotNull Project project) {
    this.project = project;
  }

  @Override
  protected void customizeCellRenderer(@NotNull JList<? extends String> list, String value, int index,
                                       boolean selected, boolean hasFocus) {
    Optional<GitRepository> repository = GtUtil.getRepositoryForRoot(project, value);
    if (repository.isPresent()) {
      render(repository.get());
    } else {
      renderMissing(value);
    }
  }

  private void render(GitRepository repository) {
    append(GtUtil.name(repository));
    StringBand url = new StringBand(" (");
    url.append(repository.getRoot().getPresentableUrl());
    url.append(")");
    append(url.toString(), GRAYED_ATTRIBUTES);
  }

  private void renderMissing(String value) {
    String path = VfsUtilCore.urlToPath(value);
    append(path, ERROR_ATTRIBUTES);
  }
}
