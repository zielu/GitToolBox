package zielu.gittoolbox.ui

import com.intellij.vcs.log.impl.HashImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import zielu.TestType
import zielu.gittoolbox.ResBundle
import zielu.gittoolbox.UtfSeq
import zielu.gittoolbox.status.BehindStatus
import zielu.gittoolbox.status.RevListCount
import java.util.stream.Stream

@Tag(TestType.FAST)
internal class StatusPresenterTest {
  private val hash = HashImpl.build("92c4b38ed6cc6f2091f454d177074fceb70d5a80")

  @ParameterizedTest
  @MethodSource("expectedZeroBehindStatusArgs")
  fun expectedZeroBehindStatus(presenter: StatusPresenter, expectedValue: String) {
    val status = presenter.behindStatus(BehindStatus.empty())
    assertThat(status).isEqualTo(expectedValue)
  }

  @ParameterizedTest
  @MethodSource("expectedBehindStatusWithDeltaArgs")
  fun expectedBehindStatusWithDelta(presenter: StatusPresenter, count: Int, delta: Int, expectedValue: String) {
    val status = presenter.behindStatus(BehindStatus.create(RevListCount.success(count, hash), delta))
    assertThat(status).isEqualTo(expectedValue)
  }

  companion object {
    @JvmStatic
    private fun expectedZeroBehindStatusArgs(): Stream<Arguments> {
      return Stream.of(
        Arguments.of(StatusPresenters.arrows, "0" + UtfSeq.ARROW_DOWN),
        Arguments.of(StatusPresenters.arrowHeads, "0" + UtfSeq.ARROWHEAD_DOWN),
        Arguments.of(StatusPresenters.text, "0 " + ResBundle.message("git.behind"))
      )
    }

    @JvmStatic
    private fun expectedBehindStatusWithDeltaArgs(): Stream<Arguments> {
      return Stream.of(
        Arguments.of(StatusPresenters.arrows, 1, 1, "1" + UtfSeq.ARROW_DOWN + " " + UtfSeq.INCREMENT + "1"),
        Arguments.of(StatusPresenters.arrows, 1, 0, "1" + UtfSeq.ARROW_DOWN + " "), // TODO: remove trailing space
        Arguments.of(StatusPresenters.arrows, 1, -1, "1" + UtfSeq.ARROW_DOWN + " " + UtfSeq.INCREMENT + "-1"),
        Arguments.of(StatusPresenters.arrowHeads, 1, 1, "1" + UtfSeq.ARROWHEAD_DOWN + " +1"),
        Arguments.of(StatusPresenters.text, 1, 1, "1 " + ResBundle.message("git.behind") + " +1")
      )
    }
  }
}
