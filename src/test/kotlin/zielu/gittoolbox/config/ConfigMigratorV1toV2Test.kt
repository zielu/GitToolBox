package zielu.gittoolbox.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ConfigMigratorV1toV2Test {

  @Test
  fun `should migrate location before status`() {
    // given
    val v1 = GitToolBoxConfig()

    // when
    val v2 = migrate(v1)

    // then
    assertDecorationParts(
      v2,
      DecorationPartType.LOCATION,
      DecorationPartType.BRANCH,
      DecorationPartType.STATUS,
      DecorationPartType.TAGS_ON_HEAD
    )
  }

  @Test
  fun `should migrate status before location`() {
    // given
    val v1 = GitToolBoxConfig()
    v1.showProjectViewStatusBeforeLocation = true

    // when
    val v2 = migrate(v1)

    // then
    assertDecorationParts(
      v2,
      DecorationPartType.BRANCH,
      DecorationPartType.STATUS,
      DecorationPartType.TAGS_ON_HEAD,
      DecorationPartType.LOCATION
    )
  }

  @Test
  fun `should migrate show tags on head disabled`() {
    // given
    val v1 = GitToolBoxConfig()
    v1.showProjectViewHeadTags = false

    // when
    val v2 = migrate(v1)

    // then
    assertDecorationParts(
      v2,
      DecorationPartType.LOCATION,
      DecorationPartType.BRANCH,
      DecorationPartType.STATUS
    )
  }

  @Test
  fun `should migrate show location path disabled`() {
    // given
    val v1 = GitToolBoxConfig()
    v1.showProjectViewLocationPath = false

    // when
    val v2 = migrate(v1)

    // then
    assertDecorationParts(
      v2,
      DecorationPartType.BRANCH,
      DecorationPartType.STATUS,
      DecorationPartType.TAGS_ON_HEAD
    )
  }

  private fun migrate(v1: GitToolBoxConfig): GitToolBoxConfig2 {
    val v2 = GitToolBoxConfig2()
    val migrator = ConfigMigratorV1toV2(v1)
    migrator.migrate(v2)
    return v2
  }

  private fun assertDecorationParts(v2: GitToolBoxConfig2, vararg types: DecorationPartType) {
    assertThat(v2.decorationParts)
      .extracting<DecorationPartType, RuntimeException> { part: DecorationPartConfig -> part.type }
      .containsExactly(*types)
  }
}
