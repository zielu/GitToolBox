package zielu.gittoolbox.blame.calculator.persistence

import com.intellij.openapi.project.Project
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Clock
import java.time.temporal.ChronoUnit

@ExtendWith(MockKExtension::class)
internal class BlameCalculationPersistenceTest {
  @MockK
  lateinit var projectMock: Project

  @Test
  fun `should remove outdated items on initialization`() {
    // given
    val now = Clock.systemUTC().instant()
    val state = BlameState()
    val fileBlame = FileBlameState(
      now.minus(30, ChronoUnit.DAYS).toEpochMilli(),
      BlameRevisionState(),
      listOf(LineState())
    )
    state.fileBlames = mapOf("/path/to/file" to fileBlame)
    val persistence = BlameCalculationPersistence(projectMock)

    // when
    persistence.loadState(state)
    persistence.initializeComponent()

    // then
    assertThat(persistence.state.fileBlames).isEmpty()
  }
}
