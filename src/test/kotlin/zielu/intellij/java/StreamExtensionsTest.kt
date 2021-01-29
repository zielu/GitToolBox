package zielu.intellij.java

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class StreamExtensionsTest {

  @Test
  fun `mapNotNull should return mapped items if not null`() {
    // given
    val stream = Stream.of(null, 1, null, 2, null, null, 3, null)

    // when
    val mappedStream = stream.mapNotNull {
      if (it != null && it % 2 == 0) {
        it.toString()
      } else {
        null
      }
    }

    // then
    assertThat(mappedStream).containsExactly("2")
  }

  @Test
  fun `toSet should collect stream items to Set`() {
    // given
    val stream = Stream.of(1, 2, 3, 3, 4, 5, 5)

    // when
    val set = stream.toSet()

    // then
    assertAll(
      { assertThat(set).isInstanceOf(Set::class.java) },
      { assertThat(set).containsOnly(1, 2, 3, 4, 5) }
    )
  }

  @ParameterizedTest
  @MethodSource("firstOrNullCases")
  fun `firstOrNull should return expected item`(stream: Stream<Int>, expectedItem: Int?) {
    // when
    val item = stream.firstOrNull { it % 2 == 0 }

    // then
    assertThat(item).isEqualTo(expectedItem)
  }

  @ParameterizedTest
  @MethodSource("singleOrNullCases")
  fun `singleOrNull should return expected item`(stream: Stream<Int>, expectedItem: Int?) {
    // when
    val item = stream.singleOrNull()

    // then
    assertThat(item).isEqualTo(expectedItem)
  }

  companion object {
    @JvmStatic
    fun firstOrNullCases(): Stream<Arguments> {
      return Stream.of(
        Arguments.of(Stream.of(1, 2, 4), 2),
        Arguments.of(Stream.of<Int>(), null),
        Arguments.of(Stream.of(1), null)
      )
    }

    @JvmStatic
    fun singleOrNullCases(): Stream<Arguments> {
      return Stream.of(
        Arguments.of(Stream.of(1, 2), null),
        Arguments.of(Stream.of<Int>(), null),
        Arguments.of(Stream.of(1), 1)
      )
    }
  }
}
