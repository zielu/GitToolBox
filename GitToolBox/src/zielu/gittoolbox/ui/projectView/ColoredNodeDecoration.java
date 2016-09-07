package zielu.gittoolbox.ui.projectView;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import zielu.gittoolbox.GitToolBoxConfig;

public class ColoredNodeDecoration extends NodeDecorationBase {

    public ColoredNodeDecoration(GitToolBoxConfig config) {
        super(config);
    }

    @Override
    public boolean apply(ProjectViewNode node, PresentationData data) {

        return true;
    }
}
