package zielu.gittoolbox;

import com.intellij.BundleBase;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ResourceBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

public final class ResBundle {
  @NonNls
  private static final String BUNDLE_NAME = "zielu.gittoolbox.ResourceBundle";
  private static Reference<ResourceBundle> bundle;

  private ResBundle() {
    throw new IllegalStateException();
  }

  @NotNull
  public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE_NAME) String key, Object... params) {
    return BundleBase.message(getBundle(), key, params);
  }

  private static ResourceBundle getBundle() {
    ResourceBundle resBundle = null;
    if (bundle != null) {
      resBundle = bundle.get();
    }
    if (resBundle == null) {
      resBundle = ResourceBundle.getBundle(BUNDLE_NAME);
      bundle = new SoftReference<>(resBundle);
    }
    return resBundle;
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
