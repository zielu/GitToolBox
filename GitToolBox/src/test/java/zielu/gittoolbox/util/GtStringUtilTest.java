package zielu.gittoolbox.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import zielu.gittoolbox.TestType;

@Tag(TestType.FAST)
class GtStringUtilTest {
  @ParameterizedTest
  @CsvSource({
      "'abc\ncba',abc",
      "'  abc\ncba','  abc'",
      "'abc  \ncba','abc  '",
      "'',''",
      ","
  })
  void firstLine(String value, String expected) {
    assertThat(GtStringUtil.firstLine(value)).isEqualTo(expected);
  }

  @ParameterizedTest
  @CsvSource({
      "'旺票运营平台\ncba',旺票运营平台",
      "'  旺票运营平台\ncba','  旺票运营平台'",
      "'旺票运营平台  \ncba','旺票运营平台  '"
  })
  void firstLineUnicode(String value, String expected) {
    assertThat(GtStringUtil.firstLine(value)).isEqualTo(expected);
  }
}