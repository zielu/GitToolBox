package zielu.junit5.intellij.extension.platform;

import com.intellij.idea.IdeaLogger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.EmptyRunnable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.EdtTestUtilKt;
import com.intellij.testFramework.PlatformTestCase;
import com.intellij.testFramework.TestRunnerUtil;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.messages.Topic;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.SwingUtilities;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import zielu.junit5.intellij.parameters.ExtensionContextParamResolver;
import zielu.junit5.intellij.parameters.ParameterHolder;

public class PlatformTestCaseExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {
  private static final Namespace NS = Namespace.create(PlatformTestCaseExtension.class);
  private static final ParameterResolver RESOLVER = new ExtensionContextParamResolver(NS);

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    PlatformTestCaseJUnit5 testCase = new PlatformTestCaseJUnit5();
    context.getStore(NS).put(PlatformTestCaseJUnit5.class, testCase);
    testCase.initialize(context);
  }

  @Override
  public void afterEach(ExtensionContext context) throws Exception {
    PlatformTestCaseJUnit5 testCase = getTestCase(context);
    testCase.destroy(context);
    context.getStore(NS).remove(PlatformTestCaseJUnit5.class);
  }

  private static PlatformTestCaseJUnit5 getTestCase(ExtensionContext context) {
    return context.getStore(NS).get(PlatformTestCaseJUnit5.class, PlatformTestCaseJUnit5.class);
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

  private static class PlatformTestCaseJUnit5 extends PlatformTestCase {

    //based on com.intellij.testFramework.PlatformTestCase.runBare
    private void initialize(ExtensionContext context) throws Exception {
      setName(getTestName(context));
      if (runInDispatchThread()) {
        TestRunnerUtil.replaceIdeEventQueueSafely();
        EdtTestUtil.runInEdtAndWait(this::setUp);
      } else {
        setUp();
      }
      ParameterHolder holder = ParameterHolder.getHolder(getStore(context));
      holder.register(Project.class, this::getProject);
      holder.register(Module.class, this::getModule);
      holder.register(PlatformTest.class, () -> getPlatformTest(context));
    }

    private String getTestName(ExtensionContext context) {
      return context.getTestMethod().map(Method::getName).orElse("testNameNA");
    }

    private Store getStore(ExtensionContext context) {
      return context.getStore(NS);
    }

    //based on com.intellij.testFramework.PlatformTestCase.runBare
    private void destroy(ExtensionContext context) throws Exception {
      if (runInDispatchThread()) {
        EdtTestUtil.runInEdtAndWait(this::tearDown);
      } else {
        tearDown();
      }

      ParameterHolder.removeHolder(getStore(context));

      if (IdeaLogger.ourErrorsOccurred != null) {
        throw IdeaLogger.ourErrorsOccurred;
      }

      // just to make sure all deferred Runnable's to finish
      waitForAllLaters();
      if (IdeaLogger.ourErrorsOccurred != null) {
        throw IdeaLogger.ourErrorsOccurred;
      }

      try {
        EdtTestUtil.runInEdtAndWait(() -> {
          cleanupApplicationCaches(getProject());
          resetAllFields();
        });
      } catch (Throwable ignored) {
        //ignored
      }
    }

    private static void waitForAllLaters() throws InterruptedException, InvocationTargetException {
      for (int i = 0; i < 3; i++) {
        SwingUtilities.invokeAndWait(EmptyRunnable.getInstance());
      }
    }

    private PlatformTest getPlatformTest(ExtensionContext extensionContext) {
      return new PlatformTest() {
        @Override
        public void execute(@NotNull Runnable test) {
          try {
            invokeTestRunnable(test);
          } catch (Exception e) {
            throw new RuntimeException("Failed to run test " + getTestName(extensionContext), e);
          }
        }

        @Override
        public <T> T executeInEdt(@NotNull Computable<T> test) {
          return EdtTestUtilKt.runInEdtAndGet(test::compute);
        }

        @Override
        public Document getDocument(@NotNull VirtualFile file) {
          return executeInEdt(() -> FileDocumentManager.getInstance().getDocument(file));
        }

        @Override
        public <L> void subscribe(@NotNull Topic<L> topic, @NotNull L listener) {
          connect().subscribe(topic, listener);
        }

        private MessageBusConnection connect() {
          PlatformTestCaseJUnit5 testCase = getTestCase(extensionContext);
          Project project = testCase.getProject();
          Store store = getStore(extensionContext);
          return store.getOrComputeIfAbsent(MessageBusConnection.class,
              type -> project.getMessageBus().connect(project), MessageBusConnection.class);
        }
      };
    }
  }
}
