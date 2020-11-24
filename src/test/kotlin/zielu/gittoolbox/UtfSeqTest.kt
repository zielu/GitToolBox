package zielu.gittoolbox

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class UtfSeqTest {

  @Test
  fun shouldReturnTwoCharactersIfCodepointWithVariation() {
    val chars = UtfSeq.fromCodepoint("2697", true)
    assertThat(chars).hasSize(2)
  }

  @Test
  fun shouldReturnOneCharacterIfCodepointWithoutVariation() {
    val chars = UtfSeq.fromCodepoint("2697", false)
    assertThat(chars).hasSize(1)
  }
}
