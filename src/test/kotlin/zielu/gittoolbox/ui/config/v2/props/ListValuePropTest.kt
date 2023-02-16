package zielu.gittoolbox.ui.config.v2.props

import com.intellij.ui.CollectionListModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import zielu.gittoolbox.config.ConfigItem

internal class ListValuePropTest {
  private val model = CollectionListModel<TestItem>()
  private var value = listOf<TestItem>()

  @Test
  fun `should return isModified true when model item was modified`() {
    // given
    val item = TestItem()
    value = listOf(item)
    val prop = ListValueProp(
      model,
      this::value
    )

    // when
    model.getElementAt(0).value = "abc"

    // then
    assertThat(prop.isModified()).isTrue
  }

  @Test
  fun `should copy items from model to value on apply`() {
    // given
    val item = TestItem()
    value = listOf(item)
    val prop = ListValueProp(
      model,
      this::value
    )

    // when
    model.getElementAt(0).value = "abc"
    val newItem = TestItem("xyz")
    model.add(newItem)
    prop.apply()

    // then
    assertThat(value).containsExactly(TestItem("abc"), newItem)
  }
}

internal data class TestItem(
  var value: String = ""
) : ConfigItem<TestItem> {

  override fun copy(): TestItem {
    return TestItem(
      value
    )
  }
}
