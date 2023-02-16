package zielu.gittoolbox.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

internal class MergedProjectConfigTest {
  private val appConfig = GitToolBoxConfig2()
  private val prjConfig = GitToolBoxConfigPrj()

  @Test
  fun `should change legacy value if legacy mode`() {
    // given
    val merged = createMerged(true)

    // when
    merged.setCommitMessageValidation(true)

    // then
    assertAll(
      { assertThat(prjConfig.commitMessageValidation).isTrue },
      { assertThat(appConfig.commitMessageValidationEnabled).isFalse() },
      { assertThat(prjConfig.commitMessageValidationOverride.enabled).isFalse() },
      { assertThat(prjConfig.commitMessageValidationOverride.value).isFalse() }
    )
  }

  @Test
  fun `should not change project if value same as app level`() {
    // given
    appConfig.commitMessageValidationEnabled = true
    val merged = createMerged(false)

    // when
    merged.setCommitMessageValidation(true)

    // then
    assertAll(
      { assertThat(merged.commitMessageValidation()).isTrue },
      { assertThat(prjConfig.commitMessageValidationOverride.enabled).isFalse },
      { assertThat(prjConfig.commitMessageValidationOverride.value).isFalse }
    )
  }

  @Test
  fun `should create project override if value different than app level`() {
    // given
    val merged = createMerged(false)

    // when
    merged.setCommitMessageValidation(true)

    // then
    assertAll(
      { assertThat(appConfig.commitMessageValidationEnabled).isFalse },
      { assertThat(prjConfig.commitMessageValidationOverride.enabled).isTrue },
      { assertThat(prjConfig.commitMessageValidationOverride.value).isTrue() }
    )
  }

  private fun createMerged(useLegacy: Boolean): MergedProjectConfig {
    return MergedProjectConfig(appConfig, prjConfig, useLegacy)
  }
}
