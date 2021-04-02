package zielu.gittoolbox.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

internal class ConfigForProjectMigrator2to3Test {
  private val appConfig = GitToolBoxConfig2()
  private val prjConfig = GitToolBoxConfigPrj()

  @Test
  fun `auto fetch override is created`() {
    // given
    prjConfig.autoFetch = false

    // when
    migrateAndAssertMigrated()

    // then
    assertAll(
      { assertThat(prjConfig.autoFetchEnabledOverride.enabled).isTrue },
      { assertThat(prjConfig.autoFetchEnabledOverride.value).isFalse }
    )
  }

  private fun migrateAndAssertMigrated() {
    val migrated = ConfigForProjectMigrator2to3(appConfig, prjConfig).migrate()
    assertThat(migrated).isTrue
  }

  @Test
  fun `auto fetch override is not created`() {
    // given
    prjConfig.autoFetch = true

    // when
    migrateAndAssertNotMigrated()

    // then
    assertAll(
      { assertThat(prjConfig.autoFetchEnabledOverride.enabled).isFalse },
      { assertThat(prjConfig.autoFetchEnabledOverride.value).isFalse }
    )
  }

  private fun migrateAndAssertNotMigrated() {
    val migrated = ConfigForProjectMigrator2to3(appConfig, prjConfig).migrate()
    assertThat(migrated).isFalse
  }
}
