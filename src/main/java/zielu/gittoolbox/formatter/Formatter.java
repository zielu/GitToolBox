package zielu.gittoolbox.formatter;

import javax.swing.Icon;

public interface Formatter {
  Formatted format(String input);

  Icon getIcon();
}
