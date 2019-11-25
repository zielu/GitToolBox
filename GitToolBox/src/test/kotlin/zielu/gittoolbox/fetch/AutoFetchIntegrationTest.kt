package zielu.gittoolbox.fetch

import com.intellij.openapi.project.Project
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import zielu.IntegrationTest
import zielu.junit5.intellij.extension.platform.BasePlatformTestCaseExtension

@IntegrationTest
@ExtendWith(BasePlatformTestCaseExtension::class)
internal class AutoFetchIntegrationTest {
  @Test
  fun shouldReturnAutoFetchGateway(project: Project) {
    assertThat(AutoFetchGateway.getInstance(project)).isNotNull
  }

  @Test
  fun shouldReturnAutoFetchState(project: Project) {
    assertThat(AutoFetchState.getInstance(project)).isNotNull
  }

  @Test
  fun shouldAllowFetch(project: Project) {
    // given
    val state = AutoFetchState.getInstance(project)

    // when
    assertThat(state.canAutoFetch()).isTrue()
  }
}
