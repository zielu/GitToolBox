package zielu.junit5.intellij.extension.resources;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import zielu.junit5.intellij.parameters.ExtensionContextParamResolver;
import zielu.junit5.intellij.parameters.ParameterHolder;

public class ResourcesExtension implements ParameterResolver, BeforeAllCallback {
  private static final ExtensionContext.Namespace NS = ExtensionContext.Namespace.create(ResourcesExtension.class);
  private static final ParameterResolver RESOLVER = new ExtensionContextParamResolver(NS);

  @Override
  public void beforeAll(ExtensionContext context) {
    ParameterHolder holder = ParameterHolder.getHolder(context.getStore(NS));
    holder.register(Path.class, ExternalPath.class, externalPath -> {
      String[] parts = externalPath.value();
      Path path = null;
      if (parts.length == 1) {
        path = Paths.get(parts[0]);
      } else if (parts.length > 1) {
        path = Paths.get(parts[0], Arrays.copyOfRange(parts, 1, parts.length));
      }
      if (path != null) {
        return path.normalize().toAbsolutePath();
      } else {
        return path;
      }
    });
    holder.register(TextResource.class, ResourcePath.class, TextResourceImpl::new);
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    return RESOLVER.supportsParameter(parameterContext, extensionContext);
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    return RESOLVER.resolveParameter(parameterContext, extensionContext);
  }
}
