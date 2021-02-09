package zielu.gittoolbox.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class DecorationPartTypeTest {

  @Test
  fun `should not contain UKNOWN type`() {
    assertThat(DecorationPartType.getValues()).doesNotContain(DecorationPartType.UNKNOWN)
  }
}
