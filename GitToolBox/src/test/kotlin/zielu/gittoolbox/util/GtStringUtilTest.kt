package zielu.gittoolbox.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import zielu.gittoolbox.util.GtStringUtil.firstLine

internal class GtStringUtilTest {
  @ParameterizedTest
  @CsvSource(
    "'abc\ncba',abc",
    "'  abc\ncba','  abc'",
    "'abc  \ncba','abc  '",
    "'',''",
    ",")
  fun `firstLine should return expected value`(value: String?, expected: String?) {
    assertThat(firstLine(value)).isEqualTo(expected)
  }

  @ParameterizedTest
  @CsvSource(
    "'旺票运营平台\ncba',旺票运营平台",
    "'  旺票运营平台\ncba','  旺票运营平台'",
    "'旺票运营平台  \ncba','旺票运营平台  '"
  )
  fun `firstLine should handle unicode`(value: String, expected: String) {
    assertThat(firstLine(value)).isEqualTo(expected)
  }
}
