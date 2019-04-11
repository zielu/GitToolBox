package zielu.junit5.intellij.parameters;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class ExtensionContextParamResolver implements ParameterResolver {
  private final Namespace namespace;
  private final ParameterResolver parent;

  public ExtensionContextParamResolver(Namespace namespace) {
    this(namespace, new EmptyResolver());
  }

  private ExtensionContextParamResolver(Namespace namespace, ParameterResolver parent) {
    this.namespace = namespace;
    this.parent = parent;
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    ParameterHolder holder = getHolder(extensionContext);
    boolean supports = holder != null && holder.hasValue(parameterContext);
    if (!supports) {
      supports = parent.supportsParameter(parameterContext, extensionContext);
    }
    return supports;
  }

  private ParameterHolder getHolder(ExtensionContext extensionContext) {
    return extensionContext.getStore(namespace).get(ParameterHolder.class, ParameterHolder.class);
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    ParameterHolder holder = getHolder(extensionContext);
    return holder.get(parameterContext)
        .orElseGet(() -> parent.resolveParameter(parameterContext, extensionContext));
  }

  private static final class EmptyResolver implements ParameterResolver {
    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
      return false;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
      return null;
    }
  }
}
