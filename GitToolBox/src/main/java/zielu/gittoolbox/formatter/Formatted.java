package zielu.gittoolbox.formatter;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

public final class Formatted {
  private final String text;
  private final boolean matches;

  Formatted(String text, boolean matches) {
    this.text = text;
    this.matches = matches;
  }

  public String getText() {
    return text;
  }

  public boolean matches() {
    return matches;
  }

  public boolean isDisplayable() {
    return matches && StringUtils.isNotBlank(text);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
        .append("text", "'" + text + "'")
        .append("matches", matches)
        .toString();
  }
}
