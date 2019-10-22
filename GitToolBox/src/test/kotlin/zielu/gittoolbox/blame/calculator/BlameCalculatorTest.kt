package zielu.gittoolbox.blame.calculator

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.history.VcsRevisionNumber
import com.intellij.openapi.vfs.VirtualFile
import git4idea.repo.GitRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import zielu.TestType

@Tag(TestType.FAST)
@ExtendWith(MockitoExtension::class)
internal class BlameCalculatorTest {
  @Test
  internal fun annotateReturnsNullIfNoCurrentRevisionNumber(
    @Mock projectMock: Project,
    @Mock repoMock: GitRepository,
    @Mock vFileMock: VirtualFile,
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
}
