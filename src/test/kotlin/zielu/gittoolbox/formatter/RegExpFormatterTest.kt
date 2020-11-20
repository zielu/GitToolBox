package zielu.gittoolbox.formatter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

internal class RegExpFormatterTest {
  @DisplayName("Formatting with pattern")
  @ParameterizedTest(name = "''{1}'' formatted with ''{0}'' should return ''{2}''")
  @CsvSource(
    "(.*), abc, abc",
    "(.*)b(.*), abc, ac",
    "aaa, abc, abc",
    ", abc, abc"
  )
  internal fun `format should return expected result`(pattern: String?, input: String, expected: String) {
    val formatter = RegExpFormatter.create(pattern)
    assertThat(formatter.format(input).text).isEqualTo(expected)
  }

  @DisplayName("Formatting with empty")
  @ParameterizedTest(name = "Input formatted with ''{0}'' should return input")
  @ValueSource(strings = ["   ", ""])
  internal fun `format should return input if pattern is empty`(pattern: String?) {
    val input = "abc"
    assertThat(RegExpFormatter.create(pattern).format(input).text).isEqualTo(input)
  }
}
