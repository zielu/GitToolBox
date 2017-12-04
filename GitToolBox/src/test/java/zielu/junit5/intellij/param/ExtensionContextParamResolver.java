package zielu.junit5.intellij.param;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.util.List;

public class ExtensionContextParamResolver implements ParameterResolver {
    private final Namespace namespace;
    private final ParameterResolver parent;
    private final List<Class<?>> parameterTypes;

    public ExtensionContextParamResolver(Namespace namespace, Class<?> first, Class<?>... others) {
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

    public ExtensionContextParamResolver(Namespace namespace, ParameterResolver parent, Class<?> first, Class<?>... others) {
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

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterTypes.stream().filter(type -> supportsParameter(parameterContext, extensionContext))
                .map(type -> extensionContext.getStore(namespace).get(type, type))
                .map(Object.class::cast)
                .findFirst().orElseGet(() -> parent.resolveParameter(parameterContext, extensionContext));
    }

    private  <T> boolean supportsParameter(ParameterContext parameterContext, Class<T> type) {
        return type.isAssignableFrom(parameterContext.getParameter().getType());
    }
}
