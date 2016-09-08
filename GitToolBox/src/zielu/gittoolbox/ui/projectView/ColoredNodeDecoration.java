package zielu.gittoolbox.ui.projectView;

import com.google.common.base.Optional;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.util.treeView.PresentableNodeDescriptor.ColoredFragment;
import com.intellij.ui.Gray;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.FontUtil;
import git4idea.repo.GitRepository;
import java.awt.Color;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.GitToolBoxConfig;
import zielu.gittoolbox.status.GitAheadBehindCount;

public class ColoredNodeDecoration extends NodeDecorationBase {

    public ColoredNodeDecoration(@NotNull  GitToolBoxConfig config,
                                 @NotNull GitRepository repo,
                                 @NotNull Optional<GitAheadBehindCount> aheadBehind) {
        super(config, repo, aheadBehind);
    }

    private ColoredFragment makeStatusFragment(boolean prefix) {
        int style = SimpleTextAttributes.STYLE_PLAIN;
        if (config.projectViewStatusBold) {
            style |= SimpleTextAttributes.STYLE_BOLD;
        }
        if (config.projectViewStatusItalic) {
            style |= SimpleTextAttributes.STYLE_ITALIC;
        }
        Color color = config.projectViewStatusCustomColor ? config.getProjectViewStatusColor() : Gray._128;
        SimpleTextAttributes attributes = new SimpleTextAttributes(style, color);
        String status = getStatusText();
        if (prefix) {
            status = FontUtil.spaceAndThinSpace() + status;
        }
        return new ColoredFragment(status, attributes);
    }

    private SimpleTextAttributes getLocationAttributes() {
        return SimpleTextAttributes.GRAY_ATTRIBUTES;
    }

    @Override
    public boolean apply(ProjectViewNode node, PresentationData data) {
        if (config.showProjectViewLocationPath) {
            if (config.showProjectViewStatusBeforeLocation) {
                data.addText(makeStatusFragment(true));
                data.setLocationString("- " + data.getLocationString());
            } else {
                String location = data.getLocationString();
                data.setLocationString("");
                data.addText(FontUtil.spaceAndThinSpace() + location + " - " , getLocationAttributes());
                data.addText(makeStatusFragment(false));
            }
        } else {
            data.setTooltip(data.getLocationString());
            data.setLocationString("");
            data.addText(makeStatusFragment(true));
        }
        return true;
    }
}
