package zielu.gittoolbox.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

internal class ConfigMigratorV2Test {

  @Test
  fun `version 3 is migrated to 4`() {
    // given
    val config = GitToolBoxConfig2()
    val autoFetchExtra = config.extrasConfig.autoFetchEnabledOverride
    autoFetchExtra.let {
      it.enabled = true
      it.value = false
    }
    val autoFetchOnBranchExtra = config.extrasConfig.autoFetchOnBranchSwitchOverride
    autoFetchOnBranchExtra.let {
      it.enabled = true
      it.value = false
    }

    // when
    ConfigMigratorV2.migrate3To4(config)

    // then
    assertAll(
      { assertThat(config.autoFetchEnabled).isFalse },
      { assertThat(config.autoFetchOnBranchSwitch).isFalse }
    )
  }
}
