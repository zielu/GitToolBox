package zielu.gittoolbox.formatter;

import zielu.gittoolbox.IconHandle;

public class SimpleFormatter implements Formatter {
  public static final Formatter instance = new SimpleFormatter();

  private SimpleFormatter() {
  }

  @Override
  public Formatted format(String input) {
    return new Formatted(input, true);
  }

  @Override
  public IconHandle getIconHandle() {
    return IconHandle.SIMPLE_FORMATTER;
  }
}
