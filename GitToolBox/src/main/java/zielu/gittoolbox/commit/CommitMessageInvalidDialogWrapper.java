package zielu.gittoolbox.commit;

import com.intellij.openapi.ui.DialogWrapper;
import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.ResBundle;

public class CommitMessageInvalidDialogWrapper extends DialogWrapper {

  public CommitMessageInvalidDialogWrapper() {
    super(true); // use current window as parent
    init();
    setTitle(ResBundle.message("commit.message.validation.dialog.title"));
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    JPanel dialogPanel = new JPanel(new BorderLayout());
    JLabel label = new JLabel(ResBundle.message("commit.message.validation.dialog.body"));
    dialogPanel.add(label, BorderLayout.CENTER);
    return dialogPanel;
  }
}