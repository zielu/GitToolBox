package zielu.gittoolbox.blame.calculator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.Assertions.fail;

import com.intellij.execution.process.ProcessOutputTypes;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import zielu.TestType;
import zielu.junit5.intellij.extension.resources.ResourcePath;
import zielu.junit5.intellij.extension.resources.ResourcesExtension;
import zielu.junit5.intellij.extension.resources.TextResource;

@Tag(TestType.FAST)
@ExtendWith(ResourcesExtension.class)
class IncrementalBlameBuilderTest {

  @Test
  void parsedBlameOutputHasCorrectLineCount(@ResourcePath("/blame-incremental.txt") TextResource resource) {
    List<String> annotationLines = resource.getLines();
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
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private List<CommitInfo> commitInfos;

    @BeforeAll
    void beforeAll(@ResourcePath("/blame-incremental.txt") TextResource resource) {
      commitInfos = buildCommitInfos(resource.getLines());
    }

    @ParameterizedTest(name = " {0} has revision {1} at date-time {2}")
    @CsvSource({
        "1,446d9e1cb82776c773b903c76a61bab16c6c5884,2019-03-28T22:43:29,zieluuuu@gmail.com",
        "4,446d9e1cb82776c773b903c76a61bab16c6c5884,2019-03-28T22:43:29,zieluuuu@gmail.com",
        "5,8fe24a686949e63f6cd484ca87b335fdd159181c,2019-03-27T19:43:51,zieluuuu@gmail.com",
        "33,f0673181af82880cb38368890faa54144322dff1,2019-01-23T22:40:39,zieluuuu@gmail.com"
    })
    void revisionIsCorrect(int lineNumber, String expectedRevisionHash,
                           String expectedAuthorDateTime, String expectedAuthorEmail) {
      int lineIndex = lineNumber - 1;
      assertSoftly(softly -> {
        softly.assertThat(commitInfos.get(lineIndex).getRevisionNumber().asString())
            .isEqualTo(expectedRevisionHash);
        softly.assertThat(dateFormat.format(commitInfos.get(lineIndex).getAuthorDateTime()))
            .isEqualTo(expectedAuthorDateTime);
        softly.assertThat(commitInfos.get(lineIndex).getAuthorEmail()).isEqualTo(expectedAuthorEmail);
      });
    }
  }

  private List<CommitInfo> buildCommitInfos(List<String> annotationLines) {
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
