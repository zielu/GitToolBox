package zielu.gittoolbox.startup

import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.Test
import zielu.gittoolbox.config.AutoFetchExclusionConfig
import zielu.gittoolbox.config.ConfigForProjectMigrator1to2
import zielu.gittoolbox.config.GitToolBoxConfigPrj
import zielu.gittoolbox.config.RemoteConfig
import kotlin.test.assertFalse

internal class ConfigForProjectMigrator1to2Test {

  @Test
  fun `should return false if no autofetch exclusions to migrate`() {
    // given
    val config = GitToolBoxConfigPrj()
    val migrator = ConfigForProjectMigrator1to2(config)

    // when
    val migrated = migrator.migrate()

    // then
    assertFalse { migrated }
  }

  @Test
  fun `should migrate autofetch exclusions to configs`() {
    // given
    val config = GitToolBoxConfigPrj()
    config.autoFetchExclusions = listOf("root1", "root2")
    val migrator = ConfigForProjectMigrator1to2(config)

    // when
    val migrated = migrator.migrate()

    // then
    assertSoftly { softly ->
      softly.assertThat(migrated).isTrue
      softly.assertThat(config.autoFetchExclusionConfigs).hasSize(2)
    }
  }

  @Test
  fun `should migrate autofetch exclusions to configs if duplicates are present`() {
    // given
    val config = GitToolBoxConfigPrj()
    config.autoFetchExclusions = listOf("root1", "root2")
    config.autoFetchExclusionConfigs = listOf(AutoFetchExclusionConfig("root1"))
    val migrator = ConfigForProjectMigrator1to2(config)

    // when
    val migrated = migrator.migrate()

    // then
    assertSoftly { softly ->
      softly.assertThat(migrated).isTrue
      softly.assertThat(config.autoFetchExclusionConfigs).hasSize(2)
    }
  }

  @Test
  fun `should migrate autofetch exclusions to configs and keep more specific ones if duplicates are present`() {
    // given
    val config = GitToolBoxConfigPrj()
    config.autoFetchExclusions = listOf("root1", "root2")
    val exclusionConfig1 = AutoFetchExclusionConfig("root1", arrayListOf(RemoteConfig("origin")))
    val exclusionConfig11 = AutoFetchExclusionConfig("root1")
    config.autoFetchExclusionConfigs = listOf(exclusionConfig1, exclusionConfig11)
    val migrator = ConfigForProjectMigrator1to2(config)

    val expectedExclusionConfig2 = AutoFetchExclusionConfig("root2")

    // when
    val migrated = migrator.migrate()

    // then
    assertSoftly { softly ->
      softly.assertThat(migrated).isTrue
      softly.assertThat(config.autoFetchExclusionConfigs).containsExactly(exclusionConfig1, expectedExclusionConfig2)
    }
  }

  @Test
  fun `should migrate autofetch exclusions to configs and keep existing ones without migration`() {
    // given
    val config = GitToolBoxConfigPrj()
    config.autoFetchExclusions = listOf("root1", "root2")
    val exclusionConfig1 = AutoFetchExclusionConfig("root1", arrayListOf(RemoteConfig("origin")))
    val exclusionConfig2 = AutoFetchExclusionConfig("root2", arrayListOf(RemoteConfig("upstream")))
    config.autoFetchExclusionConfigs = listOf(exclusionConfig1, exclusionConfig2)
    val migrator = ConfigForProjectMigrator1to2(config)

    // when
    val migrated = migrator.migrate()

    // then
    assertSoftly { softly ->
      softly.assertThat(migrated).isFalse
      softly.assertThat(config.autoFetchExclusionConfigs).containsExactly(exclusionConfig1, exclusionConfig2)
    }
  }
}
