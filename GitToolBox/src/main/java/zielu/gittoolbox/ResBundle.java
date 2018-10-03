package zielu.gittoolbox;

import com.intellij.CommonBundle;
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
    return CommonBundle.message(getBundle(), key, params);
  }

  @NotNull
  public static String getString(@NotNull @PropertyKey(resourceBundle = BUNDLE_NAME) String key) {
    return getBundle().getString(key);
  }

  private static ResourceBundle getBundle() {
    ResourceBundle bundle = null;
    if (ResBundle.bundle != null) {
      bundle = ResBundle.bundle.get();
    }
    if (bundle == null) {
      bundle = ResourceBundle.getBundle(BUNDLE_NAME);
      ResBundle.bundle = new SoftReference<>(bundle);
    }
    return bundle;
  }

  public static String na() {
    return getString("git.na");
  }
}
