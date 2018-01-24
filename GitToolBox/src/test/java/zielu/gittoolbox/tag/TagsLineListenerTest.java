package zielu.gittoolbox.tag;

import static org.assertj.core.api.Assertions.assertThat;

import com.intellij.execution.process.ProcessOutputTypes;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class TagsLineListenerTest {
  private static final List<String> LINES = Arrays.asList(
    "(HEAD -> develop)",
    "(tag: 173.2.1, origin/master, master)",
    "(origin/develop)",
    "(tag: 173.2.0)",
    "(tag: 173.1.2)",
    "(origin/172, 172)"
  );

  private TagsLineListener listener = new TagsLineListener();

  @Test
  void shouldExtractExpectedTags() {
    LINES.forEach(line -> listener.onLineAvailable(line, ProcessOutputTypes.STDOUT));
    assertThat(listener.getTags()).containsExactly("173.2.1", "173.2.0", "173.1.2");
  }
}