package zielu.gittoolbox.tag

import com.intellij.execution.process.ProcessOutputTypes
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class TagsLineListenerTest {
  private val listener = TagsLineListener()

  @DisplayName("Extract")
  @ParameterizedTest(name = "tags={1} from ''{0}''")
  @MethodSource("linesAndExpectedTags")
  fun `should extract expected tags`(line: String?, expectedTags: Collection<String>) {
    listener.onLineAvailable(line, ProcessOutputTypes.STDOUT)
    assertThat(listener.tags).containsExactlyInAnyOrderElementsOf(expectedTags)
  }

  companion object {
    @JvmStatic
    private fun linesAndExpectedTags(): Stream<Arguments> {
      return Stream.of(
        Arguments.of("(HEAD -> develop)", emptyList<String>()),
        Arguments.of("(HEAD -> master, tag: 1.2.2, origin/master, origin/HEAD)", setOf("1.2.2")),
        Arguments.of("(tag: 173.2.2, origin/master, master)", setOf("173.2.2")),
        Arguments.of("(origin/master, master, tag: 173.2.1)", setOf("173.2.1")),
        Arguments.of("(origin/develop)", emptyList<String>()),
        Arguments.of("(tag: 173.1.2)", setOf("173.1.2")),
        Arguments.of("(tag: 173.1.2, tag: 173.2.2)", listOf("173.1.2", "173.2.2")),
        Arguments.of("(origin/172, 172)", emptyList<String>()),
        Arguments.of("", emptyList<String>()),
        Arguments.of("  ", emptyList<String>())
      )
    }
  }
}
