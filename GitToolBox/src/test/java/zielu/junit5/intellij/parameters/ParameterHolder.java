package zielu.junit5.intellij.parameters;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;

public class ParameterHolder {
  private final Map<TypeInfo, Function<ParameterContext, ?>> storage = new HashMap<>();

  public static ParameterHolder getHolder(ExtensionContext.Store store) {
    return store.getOrComputeIfAbsent(ParameterHolder.class, type -> new ParameterHolder(), ParameterHolder.class);
  }

  public static void removeHolder(ExtensionContext.Store store) {
    store.remove(ParameterHolder.class);
  }

  public <T> void register(Class<T> type, Supplier<T> supplier) {
    storage.putIfAbsent(new TypeInfo(type), context -> supplier.get());
  }

  public <T, S extends Annotation> void register(Class<T> type, Class<S> annotationType, Function<S, T> provider) {
    storage.putIfAbsent(new TypeInfo(type, annotationType),
        context -> context.findAnnotation(annotationType).map(provider)
        .orElseThrow(() -> new IllegalStateException("Missing value for " + type + ", " + annotationType)));
  }

  boolean hasValue(ParameterContext parameterContext) {
    return storage.keySet().stream()
        .anyMatch(info -> info.matches(parameterContext));
  }

  Optional<Object> get(ParameterContext parameterContext) {
    return storage.entrySet().stream()
        .filter(entry -> entry.getKey().matches(parameterContext))
        .findAny()
        .map(Map.Entry::getValue)
        .map(provider -> provider.apply(parameterContext));
  }

  private static class TypeInfo {
    private final Class<?> type;
    private final Class<? extends Annotation> annotationType;

    private TypeInfo(@NotNull Class<?> type, Class<? extends Annotation> annotationType) {
      this.type = type;
      this.annotationType = annotationType;
    }

    private TypeInfo(@NotNull Class<?> type) {
      this.type = type;
      this.annotationType = null;
    }

    boolean matches(ParameterContext parameterContext) {
      boolean typeMatch = type.isAssignableFrom(parameterContext.getParameter().getType());
      if (annotationType != null) {
        return typeMatch && parameterContext.isAnnotated(annotationType);
      }
      return typeMatch;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }

      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      TypeInfo typeInfo = (TypeInfo) o;

      return new EqualsBuilder()
          .append(type, typeInfo.type)
          .isEquals();
    }

    @Override
    public int hashCode() {
      return new HashCodeBuilder(17, 37)
          .append(type)
          .toHashCode();
    }
  }
}
