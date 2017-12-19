package zielu.junit5.intellij;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

class ExtensionContextParamResolver implements ParameterResolver {
  private final Namespace namespace;
  private final ParameterResolver parent;
  private final List<Class<?>> parameterTypes;

  ExtensionContextParamResolver(Namespace namespace, Class<?> first, Class<?>... others) {
    this(namespace, new ParameterResolver() {
      @Override
      public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return false;
      }

      @Override
      public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return null;
      }
    }, first, others);
  }

  ExtensionContextParamResolver(Namespace namespace, ParameterResolver parent, Class<?> first, Class<?>... others) {
    this.namespace = namespace;
    this.parent = parent;
    this.parameterTypes = Lists.asList(first, others);
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    boolean supports = parameterTypes.stream().anyMatch(type -> supportsParameter(parameterContext, type));
    if (!supports) {
      supports = parent.supportsParameter(parameterContext, extensionContext);
    }
    return supports;
  }

  private <T> boolean supportsParameter(ParameterContext parameterContext, Class<T> type) {
    return type.isAssignableFrom(parameterContext.getParameter().getType());
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    Optional<Object> resolved = parameterTypes.stream()
        .filter(type -> supportsParameter(parameterContext, type))
        .map(type -> extensionContext.getStore(namespace).get(type, type))
        .map(Object.class::cast)
        .findFirst();
    return resolved.orElseGet(() -> parent.resolveParameter(parameterContext, extensionContext));
  }
}
