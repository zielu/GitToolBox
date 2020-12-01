package zielu.junit5.intellij.extension.platform;

interface JUnit5Adapted {
  void setTestName(String name);

  boolean runInEdt();

  void doSetUp() throws Exception;

  void doTearDown() throws Exception;
}
