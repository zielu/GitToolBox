package zielu.gittoolbox.fetch

import com.intellij.openapi.project.Project
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import zielu.IntegrationTest
import zielu.junit5.intellij.extension.platform.LightPlatformTestCaseExtension

@IntegrationTest
@ExtendWith(LightPlatformTestCaseExtension::class)
internal class AutoFetchIntegrationTest {
  @Test
  fun `should return AutoFetchGateway`(project: Project) {
    assertThat(AutoFetchFacade.getInstance(project)).isNotNull
  }

  @Test
  fun `should return AutoFetchState`(project: Project) {
    assertThat(AutoFetchState.getInstance(project)).isNotNull
  }

  @Test
  fun `should allow fetch`(project: Project) {
    // given
    val state = AutoFetchState.getInstance(project)

    // when
    assertThat(state.canAutoFetch()).isTrue()
  }
}
