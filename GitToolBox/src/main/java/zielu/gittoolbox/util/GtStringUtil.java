package zielu.gittoolbox.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class GtStringUtil {
  private GtStringUtil() {
    throw new IllegalStateException();
  }

  @Nullable
  public static String firstLine(@Nullable String input) {
    if (input == null) {
      return null;
    } else {
      return findFirstLine(input);
    }
  }

  @NotNull
  private static String findFirstLine(@NotNull String input) {
    for (int i = 0, n = input.length(); i < n; i++) {
      int codePointAt = input.codePointAt(i);
      if (codePointAt == '\n' || codePointAt == '\r') {
        return input.substring(0, i);
      }
    }
    return input;
  }
}
