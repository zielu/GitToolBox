package zielu.gittoolbox.extension;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.AbstractExtensionPointBean;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.util.xmlb.annotations.Attribute;

public class UpdateProjectActionEP extends AbstractExtensionPointBean {
  public static final ExtensionPointName<UpdateProjectActionEP>
      POINT_NAME = ExtensionPointName.create("zielu.gittoolbox.updateProjectAction");

  @Attribute("provider")
  public String provider;

  public UpdateProjectAction instantiate() {
    return instantiateClass(provider, ApplicationManager.getApplication().getPicoContainer());
  }
}
