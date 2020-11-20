package zielu.junit5.intellij.extension.platform;

import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.TestRunnerUtil;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.extension.ExtensionContext;

class TestCaseJUnit5Adapter {
  private static final AtomicInteger DEFAULT_TEST_NAME_SEQUENCE = new AtomicInteger(1);
  private final String defaultTestNamePrefix;
  private final JUnit5Adapted adapted;

  TestCaseJUnit5Adapter(String defaultTestNamePrefix, JUnit5Adapted adapted) {
    this.defaultTestNamePrefix = defaultTestNamePrefix;
    this.adapted = adapted;
  }

  void initialize(ExtensionContext context) throws Exception {
    adapted.setTestName(getTestName(context));
    if (adapted.runInEdt()) {
      TestRunnerUtil.replaceIdeEventQueueSafely();
      EdtTestUtil.runInEdtAndWait(adapted::doSetUp);
    } else {
      adapted.doSetUp();
    }
  }

  private String getTestName(ExtensionContext context) {
    return context.getTestMethod()
               .map(this::prepareTestName)
               .orElseGet(() -> defaultTestNamePrefix + "_" + DEFAULT_TEST_NAME_SEQUENCE.getAndIncrement());
  }

  private String prepareTestName(Method testMethod) {
    String name = testMethod.getName();
    if (name.startsWith("test")) {
      return name;
    } else {
      return "test" + StringUtils.capitalize(name);
    }
  }

  void destroy(ExtensionContext context) throws Exception {
    if (adapted.runInEdt()) {
      EdtTestUtil.runInEdtAndWait(adapted::doTearDown);
    } else {
      adapted.doTearDown();
    }
  }
}
