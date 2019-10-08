package zielu.intellij.util;

import com.intellij.BundleBase;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

public final class ZResBundle {
  @NonNls
  private static final String BUNDLE_NAME = "zielu.intellij.ZResBundle";
  private static final ZBundleHolder BUNDLE_HOLDER = new ZBundleHolder(BUNDLE_NAME);

  private ZResBundle() {
    throw new IllegalStateException();
  }

  @NotNull
  public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE_NAME) String key, Object... params) {
    return BundleBase.message(BUNDLE_HOLDER.getBundle(), key, params);
  }
}
