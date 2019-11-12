package zielu.gittoolbox

import com.intellij.openapi.project.Project
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import zielu.TestType
import zielu.gittoolbox.blame.BlameCache
import zielu.gittoolbox.fetch.AutoFetchGateway
import zielu.gittoolbox.ui.DatePresenter
import zielu.gittoolbox.ui.StatusMessagesService
import zielu.gittoolbox.ui.StatusMessagesUi
import zielu.gittoolbox.ui.blame.BlamePresenter
import zielu.junit5.intellij.extension.platform.BasePlatformTestCaseExtension

@Tag(TestType.INTEGRATION)
@ExtendWith(BasePlatformTestCaseExtension::class)
internal class ExtensionsIntegrationTest {
  @Test
  fun shouldReturnStatusMessagesService() {
    assertThat(StatusMessagesService.getInstance()).isNotNull
  }

  @Test
  fun shouldReturnDatePresenter() {
    assertThat(DatePresenter.getInstance()).isNotNull
  }

  @Test
  fun shouldReturnBlamePresenter() {
    assertThat(BlamePresenter.getInstance()).isNotNull
  }

  @Test
  fun shouldReturnStatusMessagesUi() {
    assertThat(StatusMessagesUi.getInstance()).isNotNull
  }

  @Test
  fun shouldReturnAutoFetchGateway(project: Project) {
    assertThat(AutoFetchGateway.getInstance(project)).isNotNull
  }

  @Test
  fun shouldReturnBlameCache(project: Project) {
    assertThat(BlameCache.getInstance(project)).isNotNull
  }
}
