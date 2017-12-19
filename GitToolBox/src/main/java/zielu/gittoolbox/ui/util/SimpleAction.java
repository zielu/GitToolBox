package zielu.gittoolbox.ui.util;

import java.awt.event.ActionEvent;
import javax.swing.Icon;
import org.jdesktop.swingx.action.AbstractActionExt;

public class SimpleAction extends AbstractActionExt {
  public SimpleAction(Icon icon) {
    super("", icon);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    //do nothing
  }
}
