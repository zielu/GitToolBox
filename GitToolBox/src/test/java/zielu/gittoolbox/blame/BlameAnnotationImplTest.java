package zielu.gittoolbox.blame;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zielu.gittoolbox.TestType;
import zielu.gittoolbox.revision.RevisionInfo;
import zielu.gittoolbox.revision.RevisionService;

@Tag(TestType.FAST)
@ExtendWith(MockitoExtension.class)
class BlameAnnotationImplTest {
  @Mock
  private FileAnnotation fileAnnotation;
  @Mock
  private RevisionService revisionService;
  private BlameAnnotation blameAnnotation;

  @BeforeEach
  void beforeEach() {
    when(fileAnnotation.getLineCount()).thenReturn(10);
    blameAnnotation = new BlameAnnotationImpl(fileAnnotation, revisionService);
  }

  @ParameterizedTest(name = "Out of bounds line number {0} is handled")
  @ValueSource(ints = {-1, 999})
  void handlesLineNumberOutOfBounds(int lineNumber) {
    assertThat(blameAnnotation.getBlame(lineNumber)).isEqualTo(RevisionInfo.EMPTY);
  }

  @Test
  void handlesNullLineVcsRevisionNumber() {
    final int lineNumber = 1;
    when(fileAnnotation.getLineRevisionNumber(lineNumber)).thenReturn(null);

    RevisionInfo blame = blameAnnotation.getBlame(lineNumber);
    assertThat(blame).isEqualTo(RevisionInfo.EMPTY);
  }

  @Test
  void returnsCorrectRevisionInfo(@Mock VcsRevisionNumber revisionNumber, @Mock RevisionInfo revisionInfo) {
    final int lineNumber = 2;
    when(revisionInfo.getRevisionNumber()).thenReturn(revisionNumber);
    when(fileAnnotation.getLineRevisionNumber(lineNumber)).thenReturn(revisionNumber);
    when(revisionService.getForLine(fileAnnotation, lineNumber)).thenReturn(revisionInfo);

    RevisionInfo blame = blameAnnotation.getBlame(lineNumber);
    assertThat(blame).isEqualTo(revisionInfo);
  }
}