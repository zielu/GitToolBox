package zielu.gittoolbox.ui;

import static org.assertj.core.api.Assertions.assertThat;

import com.intellij.vcs.log.Hash;
import com.intellij.vcs.log.impl.HashImpl;
import java.util.stream.Stream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import zielu.gittoolbox.ResBundle;
import zielu.gittoolbox.UtfSeq;
import zielu.gittoolbox.status.BehindStatus;
import zielu.gittoolbox.status.RevListCount;

@Tag("fast")
class StatusPresenterTest {
  private static final Hash HASH = HashImpl.build("92c4b38ed6cc6f2091f454d177074fceb70d5a80");

  @ParameterizedTest
  @MethodSource("expectedZeroBehindStatusArgs")
  void expectedZeroBehindStatus(StatusPresenter presenter, String expectedValue) {
    String status = presenter.behindStatus(BehindStatus.empty());
    assertThat(status).isEqualTo(expectedValue);
  }

  private static Stream<Arguments> expectedZeroBehindStatusArgs() {
    return Stream.of(
      Arguments.of(StatusPresenters.arrows, "0" + UtfSeq.ARROW_DOWN),
      Arguments.of(StatusPresenters.arrowHeads, "0" + UtfSeq.ARROWHEAD_DOWN),
      Arguments.of(StatusPresenters.text, "0 " + ResBundle.getString("git.behind"))
    );
  }

  @ParameterizedTest
  @MethodSource("expectedBehindStatusWithDeltaArgs")
  void expectedBehindStatusWithDelta(StatusPresenter presenter, String expectedValue) {
    String status = presenter.behindStatus(BehindStatus.create(
        RevListCount.success(1, HASH), 1));
    assertThat(status).isEqualTo(expectedValue);
  }

  private static Stream<Arguments> expectedBehindStatusWithDeltaArgs() {
    return Stream.of(
      Arguments.of(StatusPresenters.arrows, "1" + UtfSeq.ARROW_DOWN + " " + UtfSeq.INCREMENT + "1"),
      Arguments.of(StatusPresenters.arrowHeads, "1" + UtfSeq.ARROWHEAD_DOWN + " +1"),
      Arguments.of(StatusPresenters.text, "1 " + ResBundle.getString("git.behind") + " +1")
    );
  }
}