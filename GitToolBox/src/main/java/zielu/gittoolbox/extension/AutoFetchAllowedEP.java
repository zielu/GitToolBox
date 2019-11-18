package zielu.gittoolbox.extension;

import com.intellij.openapi.extensions.AbstractExtensionPointBean;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.annotations.Attribute;
import org.jetbrains.annotations.NotNull;

public class AutoFetchAllowedEP extends AbstractExtensionPointBean {
  public static final ExtensionPointName<AutoFetchAllowedEP>
      POINT_NAME = ExtensionPointName.create("zielu.gittoolbox.autoFetchAllowed");

  @Attribute("provider")
  public String provider;

  public AutoFetchAllowed instantiate(@NotNull Project project) {
    return instantiateClass(provider, project.getPicoContainer());
  }
}
