package zielu.gittoolbox.formatter;

public class SimpleFormatter implements Formatter {
  public static final Formatter instance = new SimpleFormatter();

  private SimpleFormatter() {
  }

  @Override
  public Formatted format(String input) {
    return new Formatted(input, true);
  }
}
