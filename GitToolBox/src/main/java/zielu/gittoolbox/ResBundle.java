package zielu.gittoolbox;

import com.intellij.BundleBase;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;
import zielu.intellij.util.ZBundleHolder;

public final class ResBundle {
  @NonNls
  private static final String BUNDLE_NAME = "zielu.gittoolbox.ResourceBundle";
  private static final ZBundleHolder BUNDLE_HOLDER = new ZBundleHolder(BUNDLE_NAME);

  private ResBundle() {
    throw new IllegalStateException();
  }

  @NotNull
  public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE_NAME) String key, Object... params) {
    return BundleBase.message(BUNDLE_HOLDER.getBundle(), key, params);
  }

  public static String na() {
    return message("common.na");
  }

  public static String naLabel() {
    return message("common.na.label");
  }

  public static String on() {
    return message("common.on");
  }

  public static String disabled() {
    return message("common.disabled");
  }

  public static String example() {
    return message("common.example.label");
  }
}
