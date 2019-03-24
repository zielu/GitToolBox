package zielu.gittoolbox.formatter;

import zielu.gittoolbox.IconHandle;

public interface Formatter {
  Formatted format(String input);

  IconHandle getIconHandle();
}
