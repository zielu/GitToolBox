package zielu.gittoolbox.ui.config.v2.props

import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.ui.CollectionListModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import zielu.gittoolbox.config.CommitCompletionConfig
import zielu.gittoolbox.config.CommitCompletionType
import zielu.gittoolbox.config.override.CommitCompletionConfigListOverride
import zielu.intellij.test.MockItemSelectable

internal class CommitCompletionConfigListWithOverrideTest {
  private val override = AtomicBooleanProperty(false)
  private val model = CollectionListModel<CommitCompletionConfig>()
  private var appValue = listOf<CommitCompletionConfig>()
  private var prjOverride = CommitCompletionConfigListOverride()
  private val itemSelectableMock = MockItemSelectable()

  private fun createProp(): CommitCompletionConfigListWithOverride {
    return CommitCompletionConfigListWithOverride(
      model,
      override,
      this::appValue,
      this::prjOverride,
      itemSelectableMock
    )
  }

  @Test
  fun `should return isModified true when model item was modified`() {
    // given
    itemSelectableMock.selectedObjects = arrayOf(true)
    prjOverride.enabled = true
    prjOverride.values = listOf(CommitCompletionConfig(
      CommitCompletionType.PATTERN,
      ".*",
      "test"
    ))
    val prop = createProp()

    // when
    model.getElementAt(0).pattern = ".+"

    // then
    assertThat(prop.isModified()).isTrue
  }
}
