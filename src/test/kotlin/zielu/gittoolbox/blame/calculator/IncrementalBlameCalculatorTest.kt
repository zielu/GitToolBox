package zielu.gittoolbox.blame.calculator

import com.intellij.openapi.vcs.history.VcsRevisionNumber
import com.intellij.openapi.vfs.VirtualFile
import git4idea.GitRevisionNumber
import git4idea.commands.GitCommandResult
import git4idea.commands.GitLineHandler
import git4idea.repo.GitRepository
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import zielu.gittoolbox.metrics.MockMetrics
import zielu.intellij.test.MockVfsUtil

@ExtendWith(MockKExtension::class)
internal class IncrementalBlameCalculatorTest {
  private val metricsMock = MockMetrics()
  private val vFileMock: VirtualFile = MockVfsUtil.createFile("/path/to/file.txt")

  @Test
  fun `annotate returns null if no current revision number`(
    @MockK repoMock: GitRepository,
    @MockK facadeMock: BlameCalculatorFacade
  ) {
    // given
    val calculator = IncrementalBlameCalculator(facadeMock)

    // when
    val dataProvider = calculator.annotate(repoMock, vFileMock, VcsRevisionNumber.NULL)

    // then
    assertThat(dataProvider).isNull()
  }

  @Test
  fun `annotate returns result if current revision number is present and command succeeds`(
    @MockK repoMock: GitRepository,
    @MockK facadeMock: BlameCalculatorFacade,
    @RelaxedMockK gitLineHandlerMock: GitLineHandler
  ) {
    // given
    every { facadeMock.createLineHandler(repoMock) } returns gitLineHandlerMock
    val commandResult = GitCommandResult(false, 0, listOf(), listOf())
    every { facadeMock.runCommand(gitLineHandlerMock) } returns commandResult
    val timer = metricsMock.timer("test")
    every { facadeMock.annotateTimer() } returns timer
    val calculator = IncrementalBlameCalculator(facadeMock)
    val fileRevision = GitRevisionNumber("abc")

    // when
    val dataProvider = calculator.annotate(repoMock, vFileMock, fileRevision)

    // then
    assertThat(dataProvider).isNotNull
    assertThat(dataProvider!!.baseRevision).isEqualTo(fileRevision)
  }

  @Test
  fun `annotate returns null if current revision number is present and command fails`(
    @MockK repoMock: GitRepository,
    @MockK facadeMock: BlameCalculatorFacade,
    @RelaxedMockK gitLineHandlerMock: GitLineHandler
  ) {
    // given
    every { facadeMock.createLineHandler(repoMock) } returns gitLineHandlerMock
    val commandResult = GitCommandResult(false, 1, listOf("Blame failure for test"), listOf())
    every { facadeMock.runCommand(gitLineHandlerMock) } returns commandResult
    val timer = metricsMock.timer("test")
    every { facadeMock.annotateTimer() } returns timer
    val calculator = IncrementalBlameCalculator(facadeMock)

    // when
    val dataProvider = calculator.annotate(repoMock, vFileMock, GitRevisionNumber("abc"))

    // then
    assertThat(dataProvider).isNull()
  }
}
