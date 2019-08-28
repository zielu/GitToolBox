package zielu.gittoolbox.repo;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import zielu.TestType;
import zielu.junit5.intellij.extension.resources.ExternalPath;
import zielu.junit5.intellij.extension.resources.ResourcesExtension;

@Tag(TestType.FAST)
@ExtendWith(ResourcesExtension.class)
class GtConfigTest {
  private static GtConfig config;

  @BeforeAll
  static void beforeClass(@ExternalPath({".", "testData", "GtConfig", "config"}) Path testConfig) {
    config = GtConfig.load(testConfig.toFile());
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
