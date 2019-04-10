package zielu.junit5.intellij;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.base.Suppliers;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.Topic;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class IdeaMocksExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {
  private static final ExtensionContext.Namespace NS = ExtensionContext.Namespace.create(IdeaMocksExtension.class);
  private static final ParameterResolver RESOLVER = new ExtensionContextParamResolver(NS);

  @Override
  public void beforeEach(ExtensionContext context) {
    IdeaMocksImpl ideaMocks = new IdeaMocksImpl();
    Project project = mock(Project.class);
    MessageBus messageBus = mock(MessageBus.class);
    when(project.getMessageBus()).thenReturn(messageBus);
    when(messageBus.syncPublisher(any(Topic.class))).thenAnswer(invocation -> {
      Topic topic = invocation.getArgument(0);
      Class<?> listenerClass = topic.getListenerClass();
      if (ideaMocks.hasMockListener(listenerClass)) {
        return ideaMocks.getMockListener(listenerClass);
      } else {
        return ideaMocks.mockListener(listenerClass);
      }
    });
    Store store = context.getStore(NS);
    ParameterHolder holder = ParameterHolder.getHolder(store);
    holder.register(Project.class, Suppliers.ofInstance(project));
    holder.register(MessageBus.class, Suppliers.ofInstance(messageBus));
    holder.register(IdeaMocks.class, Suppliers.ofInstance(ideaMocks));
  }

  @Override
  public void afterEach(ExtensionContext context) {
    ParameterHolder.removeHolder(context.getStore(NS));
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
