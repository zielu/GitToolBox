package zielu.junit5.intellij;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.PlatformTestCase;
import com.intellij.testFramework.TestRunnerUtil;
import java.lang.reflect.Method;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class PlatformTestCaseExtension implements BeforeAllCallback, AfterAllCallback,
    BeforeEachCallback, AfterEachCallback, ParameterResolver {
  private static final Namespace NS = Namespace.create(PlatformTestCaseExtension.class);
  private static final ParameterResolver RESOLVER = new ExtensionContextParamResolver(NS,
      Project.class, Module.class);

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    PlatformTestCaseJUnit5 testCase = new PlatformTestCaseJUnit5();
    context.getStore(NS).put(PlatformTestCaseJUnit5.class, testCase);
  }

  @Override
  public void afterAll(ExtensionContext context) {
    context.getStore(NS).remove(PlatformTestCaseJUnit5.class);
  }

  private PlatformTestCaseJUnit5 getTestCase(ExtensionContext context) {
    return context.getStore(NS).get(PlatformTestCaseJUnit5.class, PlatformTestCaseJUnit5.class);
  }

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    PlatformTestCaseJUnit5 testCase = getTestCase(context);
    testCase.setName(context);
    testCase.runSetup(context);
  }

  @Override
  public void afterEach(ExtensionContext context) throws Exception {
    PlatformTestCaseJUnit5 testCase = getTestCase(context);
    testCase.runTearDown(context);
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
    private void setName(ExtensionContext context) {
      String name = context.getTestMethod().map(Method::getName).orElse("testNameNA");
      setName(name);
    }

    private void runSetup(ExtensionContext context) throws Exception {
      if (runInDispatchThread()) {
        TestRunnerUtil.replaceIdeEventQueueSafely();
        EdtTestUtil.runInEdtAndWait(this::setUp);
      } else {
        setUp();
      }
      Store store = getStore(context);
      store.put(Project.class, getProject());
      store.put(Module.class, getModule());
    }

    private Store getStore(ExtensionContext context) {
      return context.getStore(NS);
    }

    private void runTearDown(ExtensionContext context) throws Exception {
      if (runInDispatchThread()) {
        EdtTestUtil.runInEdtAndWait(this::tearDown);
      } else {
        tearDown();
      }
      Store store = getStore(context);
      store.remove(Project.class);
      store.remove(Module.class);
    }
  }
}
