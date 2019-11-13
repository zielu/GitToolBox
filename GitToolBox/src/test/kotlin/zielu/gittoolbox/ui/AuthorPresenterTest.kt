package zielu.gittoolbox.ui

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import zielu.gittoolbox.config.AuthorNameType
import zielu.gittoolbox.ui.AuthorPresenter.format

internal class AuthorPresenterTest {
  @ParameterizedTest
  @CsvSource(
    "INITIALS,Jon Snow,jon.snow@the.wall,JS",
    "LASTNAME,Jon Snow,jon.snow@the.wall,Snow",
    "FIRSTNAME,Jon Snow,jon.snow@the.wall,Jon",
    "FULL,Jon Snow,jon.snow@the.wall,Jon Snow"
  )
  fun shouldReturnExpectedValueWhenSpaceInside(
    type: AuthorNameType,
    author: String,
    email: String,
    expected: String
  ) {
    val formatted = format(type, author, email)
    assertThat(formatted).isEqualTo(expected)
  }

  @ParameterizedTest
  @CsvSource(
    "INITIALS,JonSnow,jon.snow@the.wall,J",
    "LASTNAME,JonSnow,jon.snow@the.wall,JonSnow",
    "FIRSTNAME,JonSnow,jon.snow@the.wall,JonSnow",
    "FULL,JonSnow,jon.snow@the.wall,JonSnow",
    "EMAIL,JonSnow,jon.snow@the.wall,jon.snow@the.wall",
    "EMAIL,JonSnow,,",
    "EMAIL_USER,JonSnow,jon.snow@the.wall,jon.snow",
    "EMAIL_USER,JonSnow,jon.snow.at.the.wall,jon.snow.at.the.wall"
  )
  fun shouldReturnExpectedValueWhenNoSpaceInside(
    type: AuthorNameType,
    author: String,
    email: String?,
    expected: String?
  ) {
    val formatted = format(type, author, email)
    assertThat(formatted).isEqualTo(expected)
  }
}
