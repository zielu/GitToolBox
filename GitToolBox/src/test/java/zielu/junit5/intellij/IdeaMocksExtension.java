package zielu.junit5.intellij;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class IdeaMocksExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {
  private static final ExtensionContext.Namespace NS = ExtensionContext.Namespace.create(IdeaMocksExtension.class);
  private static final ParameterResolver RESOLVER = new ExtensionContextParamResolver(NS,
      Project.class, MessageBus.class, IdeaMocks.class);

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
    store.put(Project.class, project);
    store.put(MessageBus.class, messageBus);
    store.put(IdeaMocks.class, ideaMocks);
  }

  @Override
  public void afterEach(ExtensionContext context) {
    Store store = context.getStore(NS);
    store.remove(Project.class);
    store.remove(MessageBus.class);
    store.remove(IdeaMocks.class);
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
