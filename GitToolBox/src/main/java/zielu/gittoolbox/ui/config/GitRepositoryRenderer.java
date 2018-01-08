package zielu.gittoolbox.ui.config;

import static com.intellij.ui.SimpleTextAttributes.ERROR_ATTRIBUTES;
import static com.intellij.ui.SimpleTextAttributes.GRAYED_ATTRIBUTES;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
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
      GitRepository repo = repository.get();
      append(GtUtil.name(repo));
      StringBand url = new StringBand(" (");
      url.append(repo.getRoot().getPresentableUrl());
      url.append(")");
      append(url.toString(), GRAYED_ATTRIBUTES);
    } else {
      String path = VfsUtilCore.urlToPath(value);
      append(path, ERROR_ATTRIBUTES);
    }
  }
}
