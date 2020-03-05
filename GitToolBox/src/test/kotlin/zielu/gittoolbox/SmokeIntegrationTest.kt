package zielu.gittoolbox

import com.intellij.openapi.project.Project
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import zielu.IntegrationTest
import zielu.gittoolbox.blame.BlameCache
import zielu.gittoolbox.branch.RecentBranchesService
import zielu.gittoolbox.changes.ChangesTrackerService
import zielu.gittoolbox.fetch.AutoFetchComponent
import zielu.gittoolbox.fetch.AutoFetchState
import zielu.gittoolbox.metrics.AppMetrics
import zielu.gittoolbox.metrics.ProjectMetrics
import zielu.gittoolbox.revision.RevisionService
import zielu.gittoolbox.store.WorkspaceStore
import zielu.gittoolbox.ui.DatePresenter
import zielu.gittoolbox.ui.StatusMessagesService
import zielu.gittoolbox.ui.StatusMessagesUi
import zielu.gittoolbox.ui.blame.BlamePresenter
import zielu.gittoolbox.ui.branch.BranchUiService
import zielu.gittoolbox.ui.update.UpdateProjectActionService
import zielu.junit5.intellij.extension.platform.BasePlatformTestCaseExtension

@IntegrationTest
@ExtendWith(BasePlatformTestCaseExtension::class)
internal class SmokeIntegrationTest {
  @Test
  fun `should return StatusMessagesService`() {
    assertThat(StatusMessagesService.getInstance()).isNotNull
  }

  @Test
  fun `should return DatePresenter`() {
    assertThat(DatePresenter.getInstance()).isNotNull
  }

  @Test
  fun `should return BlamePresenter`() {
    assertThat(BlamePresenter.getInstance()).isNotNull
  }

  @Test
  fun `should return StatusMessagesUi`() {
    assertThat(StatusMessagesUi.getInstance()).isNotNull
  }

  @Test
  fun `should return BlameCache`(project: Project) {
    assertThat(BlameCache.getInstance(project)).isNotNull
  }

  @Test
  fun `should return ProjectUpdateAction service`() {
    assertThat(UpdateProjectActionService.getInstance()).isNotNull
  }

  @Test
  fun `should return RevisionService`(project: Project) {
    assertThat(RevisionService.getInstance(project)).isNotNull
  }

  @Test
  fun `should return AutoFetchState`(project: Project) {
    assertThat(AutoFetchState.getInstance(project)).isNotNull
  }

  @Test
  fun `should return AutoFetchComponent`(project: Project) {
    assertThat(AutoFetchComponent.getInstance(project)).isNotNull
  }

  @Test
  fun `should return ChangesTrackerService`(project: Project) {
    assertThat(ChangesTrackerService.getInstance(project)).isNotNull
  }

  @Test
  fun `should return AppMetrics`() {
    assertThat(AppMetrics.getInstance()).isNotNull
  }

  @Test
  fun `should return ProjectMetrics`(project: Project) {
    assertThat(ProjectMetrics.getInstance(project)).isNotNull
  }

  @Test
  fun `should return WorkspaceStore`(project: Project) {
    assertThat(WorkspaceStore.getInstance(project)).isNotNull
  }

  @Test
  fun `should return RecentBranchesService`(project: Project) {
    assertThat(RecentBranchesService.getInstance(project)).isNotNull
  }

  @Test
  fun `should return BranchUiService`(project: Project) {
    assertThat(BranchUiService.getInstance(project)).isNotNull
  }
}
