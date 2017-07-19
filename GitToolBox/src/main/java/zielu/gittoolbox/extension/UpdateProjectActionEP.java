package zielu.gittoolbox.extension;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.AbstractExtensionPointBean;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.util.xmlb.annotations.Attribute;

public class UpdateProjectActionEP extends AbstractExtensionPointBean {
    public final static ExtensionPointName<UpdateProjectActionEP>
            POINT_NAME = ExtensionPointName.create("zielu.gittoolbox.updateProjectInvoker");

    @Attribute("provider")
    public String provider;

    public UpdateProjectAction instantiate() {
        try {
            return instantiate(provider, ApplicationManager.getApplication().getPicoContainer());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
