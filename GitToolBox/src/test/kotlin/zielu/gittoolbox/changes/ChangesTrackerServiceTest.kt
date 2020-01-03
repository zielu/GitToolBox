package zielu.gittoolbox.changes

import com.intellij.openapi.vcs.FilePath
import git4idea.repo.GitRepository
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import zielu.gittoolbox.metrics.MockMetrics

@ExtendWith(MockKExtension::class)
internal class ChangesTrackerServiceTest {
  private val metrics = MockMetrics()

  @MockK
  private lateinit var gatewayMock: ChangesTrackerServiceLocalGateway
  private lateinit var changesTrackerService: ChangesTrackerService

  @BeforeEach
  fun beforeEach() {
    every { gatewayMock.getNotEmptyChangeListTimer() } returns metrics.timer("1")
    changesTrackerService = ChangesTrackerServiceImpl(gatewayMock)
  }

  @Test
  fun `should update counts if change list has changes under repo`(
    @MockK filePathMock: FilePath,
    @MockK repoMock: GitRepository
  ) {
    // given
    val changeData = ChangeData(filePathMock)
    val changeListData = ChangeListData("id", listOf(changeData))
    every { gatewayMock.getRepoForPath(filePathMock) } returns repoMock
    every { gatewayMock.fireChangeCountsUpdated() } just Runs

    // when
    changesTrackerService.changeListChanged(changeListData)
    val changesCount = changesTrackerService.getChangesCount(repoMock)

    // then
    assertThat(changesCount.value).isEqualTo(1)
    verify(exactly = 1) { gatewayMock.fireChangeCountsUpdated() }
  }

  @Test
  fun `should not update counts if change list has changes outside of repo`(
    @MockK filePathMock: FilePath
  ) {
    // given
    val changeData = ChangeData(filePathMock)
    val changeListData = ChangeListData("id", listOf(changeData))
    every { gatewayMock.getRepoForPath(filePathMock) } returns null

    // when
    changesTrackerService.changeListChanged(changeListData)

    // then
    verify(exactly = 0) { gatewayMock.fireChangeCountsUpdated() }
  }

  @Test
  fun `should update counts if change is moved between lists`(
    @MockK filePathMock1: FilePath,
    @MockK filePathMock2: FilePath,
    @MockK repoMock: GitRepository
  ) {
    // given
    every { gatewayMock.getRepoForPath(filePathMock1) } returns repoMock
    every { gatewayMock.getRepoForPath(filePathMock2) } returns repoMock
    every { gatewayMock.fireChangeCountsUpdated() } just Runs

    val changeListData1 = ChangeListData("list1", listOf(ChangeData(filePathMock1), ChangeData(filePathMock2)))
    val changeListData21 = ChangeListData("list1", listOf(ChangeData(filePathMock1)))
    val changeListData22 = ChangeListData("list2", listOf(ChangeData(filePathMock2)))

    // when
    changesTrackerService.changeListChanged(changeListData1)
    changesTrackerService.changeListChanged(changeListData21)
    changesTrackerService.changeListChanged(changeListData22)

    val changesCount = changesTrackerService.getChangesCount(repoMock)

    // then
    assertThat(changesCount.value).isEqualTo(2)
    verify(exactly = 3) { gatewayMock.fireChangeCountsUpdated() }
  }
}
