package zielu.gittoolbox;

import com.intellij.CommonBundle;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ResourceBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

public class ResBundle {
    private static Reference<ResourceBundle> myBundle;

    @NonNls
    private static final String BUNDLE = "zielu.gittoolbox.ResourceBundle";

    private ResBundle() {
    }

    public static String message(@PropertyKey(resourceBundle = BUNDLE) String key, Object... params) {
        return CommonBundle.message(getBundle(), key, params);
    }

    public static String getString(@PropertyKey(resourceBundle = BUNDLE) String key) {
        return getBundle().getString(key);
    }

    private static java.util.ResourceBundle getBundle() {
        ResourceBundle bundle = null;
        if (myBundle != null) {
            bundle = myBundle.get();
        }
        if (bundle == null) {
            bundle = ResourceBundle.getBundle(BUNDLE);
            myBundle = new SoftReference<ResourceBundle>(bundle);
        }
        return bundle;
    }
}
