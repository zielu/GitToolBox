package zielu.junit5.intellij.extension.platform;

import static com.intellij.testFramework.PlatformTestCase.cleanupApplicationCaches;

import com.intellij.idea.IdeaLogger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.EmptyRunnable;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.LightPlatformTestCase;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.SwingUtilities;
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

public class LightPlatformTestCaseExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {
  private static final AtomicInteger DEFAULT_TEST_NAME_SEQUENCE = new AtomicInteger(1);
  private static final Namespace NS = Namespace.create(LightPlatformTestCaseExtension.class);
  private static final ParameterResolver RESOLVER = new ExtensionContextParamResolver(NS);

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    LightPlatformTestCaseJUnit5 testCase = new LightPlatformTestCaseJUnit5();
    context.getStore(NS).put(LightPlatformTestCaseJUnit5.class, testCase);
    testCase.initialize(context);
  }

  @Override
  public void afterEach(ExtensionContext context) throws Exception {
    LightPlatformTestCaseJUnit5 testCase = getTestCase(context);
    testCase.destroy(context);
    context.getStore(NS).remove(LightPlatformTestCaseJUnit5.class);
  }

  private static LightPlatformTestCaseJUnit5 getTestCase(ExtensionContext context) {
    return context.getStore(NS).get(LightPlatformTestCaseJUnit5.class, LightPlatformTestCaseJUnit5.class);
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

  private static class LightPlatformTestCaseJUnit5 extends LightPlatformTestCase implements JUnit5Adapted {
    private final TestCaseJUnit5Adapter adapter;

    public LightPlatformTestCaseJUnit5() {
      adapter = new TestCaseJUnit5Adapter("testLightDefaultName", this);
    }

    private void initialize(ExtensionContext context) throws Exception {
      adapter.initialize(context);
      ParameterHolder holder = ParameterHolder.getHolder(getStore(context));
      holder.register(Project.class, () -> getProject());
      holder.register(Module.class, () -> getModule());
    }

    private Store getStore(ExtensionContext context) {
      return context.getStore(NS);
    }

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
