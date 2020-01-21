package zielu.gittoolbox.startup

import com.intellij.openapi.project.Project
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import zielu.gittoolbox.config.GitToolBoxConfigExtras
import zielu.gittoolbox.config.GitToolBoxConfigPrj

@ExtendWith(MockKExtension::class)
internal class ConfigOverridesMigratorTest {
  private val projectPresentableUrl = "/this/is/path/to/project"
  @MockK
  private lateinit var projectMock: Project

  @BeforeEach
  fun beforeEach() {
    every { projectMock.presentableUrl } returns projectPresentableUrl
  }

  @Test
  fun `should override auto fetch enabled`() {
    // given
    val overrides = GitToolBoxConfigExtras()
    overrides.autoFetchEnabledOverride.enabled = true
    overrides.autoFetchEnabledOverride.value = false

    val config = GitToolBoxConfigPrj()
    val migrator = ConfigOverridesMigrator(projectMock, overrides)

    // when
    val migrated = migrator.migrate(config)

    // then
    assertSoftly { softly ->
      softly.assertThat(migrated).isTrue
      softly.assertThat(config.autoFetch).isFalse
      softly.assertThat(overrides.autoFetchEnabledOverride.applied).isNotEmpty.allSatisfy { override ->
        assertThat(override.projectPath).isEqualTo(projectPresentableUrl)
      }
    }
  }

  @Test
  fun `should override auto fetch on branch switch enabled`() {
    // given
    val overrides = GitToolBoxConfigExtras()
    overrides.autoFetchOnBranchSwitchOverride.enabled = true
    overrides.autoFetchOnBranchSwitchOverride.value = false

    val config = GitToolBoxConfigPrj()
    val migrator = ConfigOverridesMigrator(projectMock, overrides)

    // when
    val migrated = migrator.migrate(config)

    // then
    assertSoftly { softly ->
      softly.assertThat(migrated).isTrue
      softly.assertThat(config.autoFetchOnBranchSwitch).isFalse
      softly.assertThat(overrides.autoFetchOnBranchSwitchOverride.applied).isNotEmpty.allSatisfy { override ->
        assertThat(override.projectPath).isEqualTo(projectPresentableUrl)
      }
    }
  }
}
