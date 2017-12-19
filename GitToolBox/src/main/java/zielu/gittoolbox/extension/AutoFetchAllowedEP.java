package zielu.gittoolbox.extension;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.AbstractExtensionPointBean;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.util.xmlb.annotations.Attribute;

public class AutoFetchAllowedEP extends AbstractExtensionPointBean {
  public static final ExtensionPointName<AutoFetchAllowedEP>
      POINT_NAME = ExtensionPointName.create("zielu.gittoolbox.autoFetchAllowed");

  @Attribute("provider")
  public String provider;

  public AutoFetchAllowed instantiate() {
    try {
      return instantiate(provider, ApplicationManager.getApplication().getPicoContainer());
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

}
