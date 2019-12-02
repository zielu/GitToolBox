package zielu.gittoolbox.extension;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.util.xmlb.annotations.Attribute;
import zielu.intellij.extensions.ZAbstractExtensionPointBean;

public class UpdateProjectActionEP extends ZAbstractExtensionPointBean {
  public static final ExtensionPointName<UpdateProjectActionEP>
      POINT_NAME = ExtensionPointName.create("zielu.gittoolbox.updateProjectAction");

  @Attribute("provider")
  public String provider;

  public UpdateProjectAction instantiate() {
    return createInstance(provider);
  }
}
