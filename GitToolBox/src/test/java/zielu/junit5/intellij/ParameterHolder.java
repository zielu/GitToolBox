package zielu.junit5.intellij;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Suppliers;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;

class ParameterHolder {
  private final Map<TypeInfo<?>, Supplier<?>> storage = new HashMap<>();

  static ParameterHolder getHolder(ExtensionContext.Store store) {
    return store.getOrComputeIfAbsent(ParameterHolder.class, type -> new ParameterHolder(), ParameterHolder.class);
  }

  static void removeHolder(ExtensionContext.Store store) {
    store.remove(ParameterHolder.class);
  }

  <T> void register(Class<T> type, Supplier<T> supplier) {
    boolean newValue = storage.put(new TypeInfo<>(type), Suppliers.memoize(supplier::get)) == null;
    checkState(newValue, "Value for " + type.getName() + " is already registered");
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
        .map(Supplier::get);
  }

  private static class TypeInfo<T> {
    private final Class<T> type;

    private TypeInfo(Class<T> type) {
      this.type = type;
    }

    boolean matches(ParameterContext parameterContext) {
      return type.equals(parameterContext.getParameter().getType());
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }

      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      TypeInfo<?> typeInfo = (TypeInfo<?>) o;

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
