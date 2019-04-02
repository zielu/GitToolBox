package zielu.gittoolbox.blame.calculator;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.Assertions.fail;

import com.google.common.base.Charsets;
import com.intellij.execution.process.ProcessOutputTypes;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class IncrementalBlameBuilderTest {
  private static List<String> annotationLines;

  @BeforeAll
  static void beforeAll() throws IOException {
    Path annotationOutput = Paths.get(".", "testData", "blame-incremental.txt");
    annotationLines = Files.readAllLines(annotationOutput, Charsets.UTF_8);
  }

  @Test
  void parseBlameOutput() {
    IncrementalBlameBuilder calculator = new IncrementalBlameBuilder();
    for (int i = 0; i < annotationLines.size(); i++) {
      try {
        calculator.onLineAvailable(annotationLines.get(i), ProcessOutputTypes.STDOUT);
      } catch (Exception e) {
        fail("Failed at line " + (i + 1), e);
      }
    }
    List<CommitInfo> commitInfos = calculator.buildLineInfos();
    assertSoftly(softly -> {
      softly.assertThat(commitInfos.size()).isEqualTo(33);
    });
  }
}