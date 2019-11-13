package zielu.gittoolbox.status.behindtracker

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import zielu.gittoolbox.status.Status

internal class StatusTest {
  @ParameterizedTest
  @CsvSource(
    "SUCCESS,true",
    "NO_REMOTE,true",
    "CANCEL,false",
    "FAILURE,false"
  )
  fun isValidShouldReturnExpectedValue(status: Status, expected: Boolean) {
    assertThat(status.isValid()).isEqualTo(expected)
  }
}
