package zielu.gittoolbox.changes

import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.ContentRevision
import com.intellij.openapi.vcs.changes.LocalChangeList
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class ChangeListDataTest {
  private val changeListId = "id"
  @MockK
  private lateinit var localChangeListMock: LocalChangeList

  @BeforeEach
  fun beforeEach() {
    every { localChangeListMock.id } returns changeListId
  }

  @Test
  fun `should create from local change list if no changes`() {
    // given
    every { localChangeListMock.changes } returns listOf()

    // when
    val changeListData = ChangeListData(localChangeListMock)

    // then
    assertSoftly { softly ->
      softly.assertThat(changeListData.id).isEqualTo(changeListId)
      softly.assertThat(changeListData.changes).isEmpty()
    }
  }

  @Test
  fun `should create from local change list if some changes`(
    @MockK afterRevisionMock: ContentRevision,
    @MockK filePathMock: FilePath,
    @MockK change1Mock: Change,
    @MockK change2Mock: Change
  ) {
    // given
    every { afterRevisionMock.file } returns filePathMock
    every { change1Mock.afterRevision } returns afterRevisionMock
    every { change2Mock.afterRevision } returns afterRevisionMock
    every { localChangeListMock.changes } returns listOf(change1Mock, change2Mock)

    // when
    val changeListData = ChangeListData(localChangeListMock)

    // then
    assertSoftly { softly ->
      softly.assertThat(changeListData.hasChanges).isTrue
      softly.assertThat(changeListData.changes).hasSize(2)
    }
  }
}
