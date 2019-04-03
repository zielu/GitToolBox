package zielu.gittoolbox.blame.calculator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import com.google.common.base.Charsets;
import com.intellij.execution.process.ProcessOutputTypes;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class IncrementalBlameBuilderTest {
  private static List<String> annotationLines;

  @BeforeAll
  static void beforeAll() throws IOException {
    Path annotationOutput = Paths.get(".", "testData", "blame-incremental.txt");
    annotationLines = Files.readAllLines(annotationOutput, Charsets.UTF_8);
  }

  @Test
  void parsedBlameOutputHasCorrectLineCount() {
    IncrementalBlameBuilder calculator = new IncrementalBlameBuilder();
    for (int i = 0; i < annotationLines.size(); i++) {
      try {
        calculator.onLineAvailable(annotationLines.get(i), ProcessOutputTypes.STDOUT);
      } catch (Exception e) {
        fail("Failed at line " + (i + 1), e);
      }
    }
    List<CommitInfo> commitInfos = calculator.buildLineInfos();
    assertThat(commitInfos.size()).isEqualTo(33);
  }

  @Nested
  @DisplayName("commit info at line")
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  class CommitInfoAtLine {
    private List<CommitInfo> commitInfos;

    @BeforeAll
    void beforeAll() {
      commitInfos = buildCommitInfos();
    }

    @ParameterizedTest(name = " {0} has revision {1}")
    @CsvSource({
        "1,446d9e1cb82776c773b903c76a61bab16c6c5884",
        "4,446d9e1cb82776c773b903c76a61bab16c6c5884",
        "5,8fe24a686949e63f6cd484ca87b335fdd159181c",
        "33,f0673181af82880cb38368890faa54144322dff1"
    })
    void revisionIsCorrect(int lineNumber, String expectedRevisionHash) {
      assertThat(commitInfos.get(lineNumber - 1).getRevisionNumber().asString()).isEqualTo(expectedRevisionHash);
    }
  }

  private List<CommitInfo> buildCommitInfos() {
    IncrementalBlameBuilder calculator = new IncrementalBlameBuilder();
    for (int i = 0; i < annotationLines.size(); i++) {
      try {
        calculator.onLineAvailable(annotationLines.get(i), ProcessOutputTypes.STDOUT);
      } catch (Exception e) {
        fail("Failed at line " + (i + 1), e);
      }
    }
    return calculator.buildLineInfos();
  }
}