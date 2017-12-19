package zielu.gittoolbox.repo;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("fast")
class GtConfigTest {
  private static GtConfig config;

  @BeforeAll
  static void beforeClass() {
    File currentDir = new File(".").getAbsoluteFile();
    File testConfig = new File(currentDir, "testData" + File.separator + "GtConfig" + File.separator + "config");
    config = GtConfig.load(testConfig);
  }

  @Test
  void isSvnRemote() throws Exception {
    assertTrue(config.isSvnRemote("svn"));
  }

  @Test
  void isRegularRemote() throws Exception {
    assertFalse(config.isSvnRemote("origin"));
  }
}
