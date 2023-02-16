package zielu.junit5.intellij.extension.platform;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
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
import zielu.junit5.intellij.util.TestUtil;

public class BasePlatformTestCaseExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {
  private static final Namespace NS = Namespace.create(BasePlatformTestCaseExtension.class);
  private static final ParameterResolver RESOLVER = new ExtensionContextParamResolver(NS);

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    BasePlatformTestCaseJUnit5 testCase = new BasePlatformTestCaseJUnit5();
    context.getStore(NS).put(BasePlatformTestCaseJUnit5.class, testCase);
    testCase.initialize(context);
  }

  @Override
  public void afterEach(ExtensionContext context) throws Exception {
    BasePlatformTestCaseJUnit5 testCase = getTestCase(context);
    testCase.destroy(context);
    context.getStore(NS).remove(BasePlatformTestCaseJUnit5.class);
  }

  private static BasePlatformTestCaseJUnit5 getTestCase(ExtensionContext context) {
    return context.getStore(NS).get(BasePlatformTestCaseJUnit5.class, BasePlatformTestCaseJUnit5.class);
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

  private static class BasePlatformTestCaseJUnit5 extends BasePlatformTestCase implements JUnit5Adapted {
    private final TestCaseJUnit5Adapter adapter;
    private BasePlatformTestDataPath testDataPath;

    public BasePlatformTestCaseJUnit5() {
      adapter = new TestCaseJUnit5Adapter("testBaseDefaultName", this);
    }

    private void initialize(ExtensionContext context) throws Exception {
      testDataPath = getTestDataPathAnnotation(context);
      adapter.initialize(context);
      ParameterHolder holder = ParameterHolder.getHolder(getStore(context));
      holder.register(Project.class, this::getProject);
      holder.register(Module.class, this::getModule);
      holder.register(CodeInsightTestFixture.class, () -> myFixture);
    }

    @Override
    protected String getTestDataPath() {
      if (testDataPath != null) {
        return TestUtil.resolvePathFromParts(testDataPath.value()).toString();
      } else {
        return super.getTestDataPath();
      }
    }

    private BasePlatformTestDataPath getTestDataPathAnnotation(ExtensionContext context) {
      return context.getTestMethod()
                 .map(method -> method.getAnnotation(BasePlatformTestDataPath.class))
                 .orElse(null);
    }

    private Store getStore(ExtensionContext context) {
      return context.getStore(NS);
    }

    private void destroy(ExtensionContext context) throws Exception {
      adapter.destroy(context);

      ParameterHolder.removeHolder(getStore(context));
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
      super.setUp();
    }

    @Override
    public void doTearDown() throws Exception {
      super.tearDown();
    }
  }
}
