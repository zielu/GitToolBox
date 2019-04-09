package zielu.gittoolbox.util;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class AppUtil {
  private AppUtil() {
    //do nothing
  }

  @NotNull
  public static <T> Optional<T> getExistingServiceInstance(@NotNull Project project, Class<T> serviceType) {
    return Optional.ofNullable(ServiceManager.getServiceIfCreated(project, serviceType));
  }

  @NotNull
  public static <T> T getServiceInstance(@NotNull Project project, Class<T> serviceType) {
    return ServiceManager.getService(project, serviceType);
  }

  @NotNull
  public static <T> T getServiceInstance(Class<T> serviceType) {
    return ServiceManager.getService(serviceType);
  }

  @NotNull
  public static <T> T getComponent(@NotNull Project project, Class<T> componentType) {
    return project.getComponent(componentType);
  }
}
