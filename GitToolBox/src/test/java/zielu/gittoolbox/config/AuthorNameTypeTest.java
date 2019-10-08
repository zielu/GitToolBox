package zielu.gittoolbox.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import zielu.TestType;

@Tag(TestType.FAST)
class AuthorNameTypeTest {

  @ParameterizedTest
  @CsvSource({
      "INITIALS,Jon Snow,JS",
      "LASTNAME,Jon Snow,Snow",
      "FIRSTNAME,Jon Snow,Jon",
      "FULL,Jon Snow,Jon Snow"
  })
  void shouldReturnExpectedValueWhenSpaceInside(AuthorNameType type, String value, String expected) {
    String shortened = type.shorten(value);
    assertThat(shortened).isEqualTo(expected);
  }

  @ParameterizedTest
  @CsvSource({
      "INITIALS,JonSnow,J",
      "LASTNAME,JonSnow,JonSnow",
      "FIRSTNAME,JonSnow,JonSnow",
      "FULL,JonSnow,JonSnow"
  })
  void shouldReturnExpectedValueWhenNoSpaceInside(AuthorNameType type, String value, String expected) {
    String shortened = type.shorten(value);
    assertThat(shortened).isEqualTo(expected);
  }
}