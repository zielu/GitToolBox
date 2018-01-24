package zielu.gittoolbox.tag;

import static org.assertj.core.api.Assertions.assertThat;

import com.intellij.execution.process.ProcessOutputTypes;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TagsLineListenerTest {
  private TagsLineListener listener = new TagsLineListener();

  @DisplayName("Extract")
  @ParameterizedTest(name = "tags={1} from ''{0}''")
  @MethodSource("linesAndExpectedTags")
  void shouldExtractExpectedTags(String line, Collection<String> expectedTags) {
    listener.onLineAvailable(line, ProcessOutputTypes.STDOUT);
    assertThat(listener.getTags()).containsOnlyElementsOf(expectedTags);
  }

  private static Stream<Arguments> linesAndExpectedTags() {
    return Stream.of(
        Arguments.of("(HEAD -> develop)", Collections.emptyList()),
        Arguments.of("(HEAD -> master, tag: 1.2.2, origin/master, origin/HEAD)", Collections.singleton("1.2.2")),
        Arguments.of("(tag: 173.2.2, origin/master, master)", Collections.singleton("173.2.2")),
        Arguments.of("(origin/master, master, tag: 173.2.1)", Collections.singleton("173.2.1")),
        Arguments.of("(origin/develop)", Collections.emptyList()),
        Arguments.of("(tag: 173.1.2)", Collections.singleton("173.1.2")),
        Arguments.of("(tag: 173.1.2, tag: 173.2.2)", Arrays.asList("173.1.2", "173.2.2")),
        Arguments.of("(origin/172, 172)", Collections.emptyList()),
        Arguments.of("", Collections.emptyList()),
        Arguments.of("  ", Collections.emptyList())
    );
  }
}
