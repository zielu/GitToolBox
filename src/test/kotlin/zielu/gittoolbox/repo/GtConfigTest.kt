package zielu.gittoolbox.repo

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import zielu.junit5.intellij.extension.resources.ExternalPath
import zielu.junit5.intellij.extension.resources.ResourcesExtension
import java.nio.file.Path
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExtendWith(ResourcesExtension::class)
internal class GtConfigTest {
  companion object {
    private lateinit var config: GtConfig

    @BeforeAll
    @JvmStatic
    fun beforeClass(@ExternalPath(".", "testData", "GtConfig", "config") testConfig: Path) {
      config = GtConfig.load(testConfig.toFile())
    }
  }

  @Test
  @Throws(Exception::class)
  fun isSvnRemote() {
    assertTrue { config.isSvnRemote("svn") }
  }

  @Test
  @Throws(Exception::class)
  fun isRegularRemote() {
    assertFalse { config.isSvnRemote("origin") }
  }
}
