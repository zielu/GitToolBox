package zielu.gittoolbox.ui.projectview;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;

public interface NodeDecoration {
  NodeDecoration noop = new NodeDecoration() {
    @Override
    public boolean apply(ProjectViewNode node, PresentationData data) {
      return false;
    }
  };

  boolean apply(ProjectViewNode node, PresentationData data);
}
