package zielu.gittoolbox.blame.calculator

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.history.VcsRevisionNumber
import git4idea.GitRevisionNumber
import git4idea.commands.GitCommandResult
import git4idea.commands.GitLineHandler
import git4idea.repo.GitRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import zielu.TestType
import zielu.intellij.test.MockVfsUtil

@Tag(TestType.FAST)
@ExtendWith(MockitoExtension::class)
internal class BlameCalculatorTest {
  private val vFileMock = MockVfsUtil.createFile("/path/to/file.txt")

  @Test
  internal fun annotateReturnsNullIfNoCurrentRevisionNumber(
    @Mock projectMock: Project,
    @Mock repoMock: GitRepository,
    @Mock gatewayMock: BlameCalculatorLocalGateway
  ) {
    // given
    `when`(gatewayMock.getCurrentRevisionNumber(vFileMock)).thenReturn(VcsRevisionNumber.NULL)
    val calculator = BlameCalculator(projectMock, gatewayMock)

    // when
    val dataProvider = calculator.annotate(repoMock, vFileMock)

    // then
    assertThat(dataProvider).isNull()
  }

  @Test
  internal fun annotateReturnsResultIfCurrentRevisionNumberPresentAndCommandSuccess(
    @Mock projectMock: Project,
    @Mock repoMock: GitRepository,
    @Mock gatewayMock: BlameCalculatorLocalGateway,
    @Mock gitLineHandlerMock: GitLineHandler
  ) {
    // given
    `when`(gatewayMock.getCurrentRevisionNumber(vFileMock)).thenReturn(GitRevisionNumber("abc"))
    `when`(gatewayMock.createLineHandler(repoMock)).thenReturn(gitLineHandlerMock)
    val commandResult = GitCommandResult(false, 0, listOf(), listOf())
    `when`(gatewayMock.runCommand(gitLineHandlerMock)).thenReturn(commandResult)
    val calculator = BlameCalculator(projectMock, gatewayMock)

    // when
    val dataProvider = calculator.annotate(repoMock, vFileMock)

    // then
    assertThat(dataProvider).isNotNull
  }

  @Test
  internal fun annotateReturnsNullIfCurrentRevisionNumberPresentAndCommandFailed(
    @Mock projectMock: Project,
    @Mock repoMock: GitRepository,
    @Mock gatewayMock: BlameCalculatorLocalGateway,
    @Mock gitLineHandlerMock: GitLineHandler
  ) {
    // given
    `when`(gatewayMock.getCurrentRevisionNumber(vFileMock)).thenReturn(GitRevisionNumber("abc"))
    `when`(gatewayMock.createLineHandler(repoMock)).thenReturn(gitLineHandlerMock)
    val commandResult = GitCommandResult(false, 1, listOf(), listOf())
    `when`(gatewayMock.runCommand(gitLineHandlerMock)).thenReturn(commandResult)
    val calculator = BlameCalculator(projectMock, gatewayMock)

    // when
    val dataProvider = calculator.annotate(repoMock, vFileMock)

    // then
    assertThat(dataProvider).isNull()
  }
}
