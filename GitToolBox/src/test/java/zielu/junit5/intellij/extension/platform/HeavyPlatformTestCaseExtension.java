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
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.messages.Topic;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;
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

public class HeavyPlatformTestCaseExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {
  private static final AtomicInteger DEFAULT_TEST_NAME_SEQUENCE = new AtomicInteger(1);
  private static final Namespace NS = Namespace.create(HeavyPlatformTestCaseExtension.class);
  private static final ParameterResolver RESOLVER = new ExtensionContextParamResolver(NS);

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    HeavyPlatformTestCaseJUnit5 testCase = new HeavyPlatformTestCaseJUnit5();
    context.getStore(NS)
        .put(HeavyPlatformTestCaseJUnit5.class, testCase);
    testCase.initialize(context);
  }

  @Override
  public void afterEach(ExtensionContext context) throws Exception {
    HeavyPlatformTestCaseJUnit5 testCase = getTestCase(context);
    testCase.destroy(context);
    context.getStore(NS)
        .remove(HeavyPlatformTestCaseJUnit5.class);
  }

  private static HeavyPlatformTestCaseJUnit5 getTestCase(ExtensionContext context) {
    return context.getStore(NS)
               .get(HeavyPlatformTestCaseJUnit5.class, HeavyPlatformTestCaseJUnit5.class);
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

  private static class HeavyPlatformTestCaseJUnit5 extends PlatformTestCase implements JUnit5Adapted {
    private final TestCaseJUnit5Adapter adapter;

    public HeavyPlatformTestCaseJUnit5() {
      adapter = new TestCaseJUnit5Adapter("testHeavyDefaultName", this);
    }

    //based on com.intellij.testFramework.HeavyPlatformTestCase.runBare
    private void initialize(ExtensionContext context) throws Exception {
      adapter.initialize(context);
      ParameterHolder holder = ParameterHolder.getHolder(getStore(context));
      holder.register(Project.class, this::getProject);
      holder.register(Module.class, this::getModule);
      holder.register(PlatformTest.class, () -> getPlatformTest(context));
    }

    private Store getStore(ExtensionContext context) {
      return context.getStore(NS);
    }

    //based on com.intellij.testFramework.HeavyPlatformTestCase.runBare
    private void destroy(ExtensionContext context) throws Exception {
      adapter.destroy(context);

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

    private String getTestName(ExtensionContext context) {
      return context.getTestMethod()
                 .map(Method::getName)
                 .orElseGet(this::getName);
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
          HeavyPlatformTestCaseJUnit5 testCase = getTestCase(extensionContext);
          Project project = testCase.getProject();
          Store store = getStore(extensionContext);
          return store.getOrComputeIfAbsent(MessageBusConnection.class,
              type -> project.getMessageBus()
                          .connect(project), MessageBusConnection.class);
        }
      };
    }

    @Override
    public void setTestName(String name) {
      setName(name);
    }

    @Override
    public boolean runInEdt() {
      return runInDispatchThread();
    }

    @Override
    public void doSetUp() throws Exception {
      setUp();
    }

    @Override
    public void doTearDown() throws Exception {
      tearDown();
    }
  }
}
