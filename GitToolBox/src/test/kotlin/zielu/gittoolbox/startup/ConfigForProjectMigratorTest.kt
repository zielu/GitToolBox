package zielu.gittoolbox.startup

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import zielu.TestType
import zielu.gittoolbox.config.AutoFetchExclusionConfig
import zielu.gittoolbox.config.GitToolBoxConfigPrj
import zielu.gittoolbox.config.RemoteConfig

@Tag(TestType.FAST)
internal class ConfigForProjectMigratorTest {

  @Test
  internal fun shouldReturnFalseIfNoAutofetchExclusionsToMigrate() {
    // given
    val config = GitToolBoxConfigPrj()
    val migrator = ConfigForProjectMigrator(config)

    // when
    val migrated = migrator.migrate()

    // then
    assertThat(migrated).isFalse()
  }

  @Test
  internal fun shouldMigrateAutofetchExclusionsToConfigs() {
    // given
    val config = GitToolBoxConfigPrj()
    config.autoFetchExclusions = listOf("root1", "root2")
    val migrator = ConfigForProjectMigrator(config)

    // when
    val migrated = migrator.migrate()

    // then
    assertSoftly { softly ->
      softly.assertThat(migrated).isTrue
      softly.assertThat(config.autoFetchExclusionConfigs).hasSize(2)
    }
  }

  @Test
  internal fun shouldMigrateAutofetchExclusionsToConfigsIfDuplicatesPresent() {
    // given
    val config = GitToolBoxConfigPrj()
    config.autoFetchExclusions = listOf("root1", "root2")
    config.autoFetchExclusionConfigs = listOf(AutoFetchExclusionConfig("root1"))
    val migrator = ConfigForProjectMigrator(config)

    // when
    val migrated = migrator.migrate()

    // then
    assertSoftly { softly ->
      softly.assertThat(migrated).isTrue
      softly.assertThat(config.autoFetchExclusionConfigs).hasSize(2)
    }
  }

  @Test
  internal fun shouldMigrateAutofetchExclusionsToConfigsAndKeepMoreSpecificOnesIfDuplicatesPresent() {
    // given
    val config = GitToolBoxConfigPrj()
    config.autoFetchExclusions = listOf("root1", "root2")
    val exclusionConfig1 = AutoFetchExclusionConfig("root1", arrayListOf(RemoteConfig("origin")))
    val exclusionConfig11 = AutoFetchExclusionConfig("root1")
    config.autoFetchExclusionConfigs = listOf(exclusionConfig1, exclusionConfig11)
    val migrator = ConfigForProjectMigrator(config)

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
  internal fun shouldMigrateAutofetchExclusionsToConfigsAndKeepExistingOnesWithoutMigration() {
    // given
    val config = GitToolBoxConfigPrj()
    config.autoFetchExclusions = listOf("root1", "root2")
    val exclusionConfig1 = AutoFetchExclusionConfig("root1", arrayListOf(RemoteConfig("origin")))
    val exclusionConfig2 = AutoFetchExclusionConfig("root2", arrayListOf(RemoteConfig("upstream")))
    config.autoFetchExclusionConfigs = listOf(exclusionConfig1, exclusionConfig2)
    val migrator = ConfigForProjectMigrator(config)

    // when
    val migrated = migrator.migrate()

    // then
    assertSoftly { softly ->
      softly.assertThat(migrated).isFalse
      softly.assertThat(config.autoFetchExclusionConfigs).containsExactly(exclusionConfig1, exclusionConfig2)
    }
  }
}
