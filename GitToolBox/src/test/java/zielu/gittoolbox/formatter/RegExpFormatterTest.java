package zielu.gittoolbox.formatter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@Tag("fast")
class RegExpFormatterTest {
  @DisplayName("Formatting with pattern")
  @ParameterizedTest(name = "''{1}'' formatted with ''{0}'' should return ''{2}''")
  @CsvSource({
      "(.*), abc, abc",
      "(.*)b(.*), abc, ac",
      "aaa, abc, abc",
      ", abc, abc"
  })
  void formatShouldReturnExpectedResult(String pattern, String input, String expected) {
    RegExpFormatter formatter = RegExpFormatter.create(pattern);
    assertThat(formatter.format(input).getText()).isEqualTo(expected);
  }

  @DisplayName("Formatting with empty")
  @ParameterizedTest(name = "Input formatted with ''{0}'' should return input")
  @ValueSource(strings = {"   ", ""})
  void formatShouldReturnInputIfPatternEmpty(String pattern) {
    final String input = "abc";
    assertThat(RegExpFormatter.create(pattern).format(input).getText()).isEqualTo(input);
  }
}
