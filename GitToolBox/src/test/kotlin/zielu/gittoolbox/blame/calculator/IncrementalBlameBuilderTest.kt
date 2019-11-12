package zielu.gittoolbox.blame.calculator

import com.intellij.execution.process.ProcessOutputTypes
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import zielu.TestType
import zielu.junit5.intellij.extension.resources.ResourcePath
import zielu.junit5.intellij.extension.resources.ResourcesExtension
import zielu.junit5.intellij.extension.resources.TextResource
import java.time.format.DateTimeFormatter

@Tag(TestType.FAST)
@ExtendWith(ResourcesExtension::class)
internal class IncrementalBlameBuilderTest {
  @Test
  internal fun parsedBlameOutputHasCorrectLineCount(
    @ResourcePath("/blame-incremental.txt") resource: TextResource
  ) {
    val annotationLines: MutableList<String> = resource.lines
    val calculator = IncrementalBlameBuilder()
    for (i in annotationLines.indices) {
      try {
        calculator.onLineAvailable(annotationLines[i], ProcessOutputTypes.STDOUT)
      } catch (e: Exception) {
        Assertions.fail<Any>("Failed at line " + (i + 1), e)
      }
    }
    val commitInfos = calculator.buildLineInfos()
    assertThat(commitInfos.size).isEqualTo(33)
  }

  @Nested
  @DisplayName("commit info at line")
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  internal inner class CommitInfoAtLine {
    private val dateFormat = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private lateinit var commitInfos: List<CommitInfo>

    @BeforeAll
    internal fun beforeAll(@ResourcePath("/blame-incremental.txt") resource: TextResource) {
      commitInfos = buildCommitInfos(resource.lines)
    }

    @ParameterizedTest(name = " {0} has revision {1} at date-time {2}")
    @CsvSource(
      "1,446d9e1cb82776c773b903c76a61bab16c6c5884,2019-03-28T22:43:29,Lukasz Zielinski,zieluuuu@gmail.com",
      "4,446d9e1cb82776c773b903c76a61bab16c6c5884,2019-03-28T22:43:29,Lukasz Zielinski,zieluuuu@gmail.com",
      "5,8fe24a686949e63f6cd484ca87b335fdd159181c,2019-03-27T19:43:51,Lukasz Zielinski,zieluuuu@gmail.com",
      "33,f0673181af82880cb38368890faa54144322dff1,2019-01-23T22:40:39,Lukasz Zielinski,zieluuuu@gmail.com"
    )
    internal fun revisionIsCorrect(
      lineNumber: Int,
      expectedRevisionHash: String,
      expectedAuthorDateTime: String,
      expectedAuthorName: String,
      expectedAuthorEmail: String
    ) {
      val lineIndex = lineNumber - 1
      val commitInfo = commitInfos[lineIndex]
      assertSoftly { softly ->
        softly.assertThat(commitInfo.revisionNumber.asString()).isEqualTo(expectedRevisionHash)
        softly.assertThat(dateFormat.format(commitInfo.authorDateTime)).isEqualTo(expectedAuthorDateTime)
        softly.assertThat(commitInfo.authorName).isEqualTo(expectedAuthorName)
        softly.assertThat(commitInfo.authorEmail).isEqualTo(expectedAuthorEmail)
      }
    }

    private fun buildCommitInfos(annotationLines: List<String>): List<CommitInfo> {
      val calculator = IncrementalBlameBuilder()
      for (i in annotationLines.indices) {
        try {
          calculator.onLineAvailable(annotationLines[i], ProcessOutputTypes.STDOUT)
        } catch (e: java.lang.Exception) {
          Assertions.fail<Any>("Failed at line " + (i + 1), e)
        }
      }
      return calculator.buildLineInfos()
    }
  }
}
