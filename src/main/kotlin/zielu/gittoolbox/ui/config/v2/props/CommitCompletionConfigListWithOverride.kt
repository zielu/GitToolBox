package zielu.gittoolbox.ui.config.v2.props

import com.intellij.openapi.Disposable
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.util.Disposer
import com.intellij.ui.CollectionListModel
import zielu.gittoolbox.config.CommitCompletionConfig
import zielu.gittoolbox.config.override.CommitCompletionConfigListOverride
import zielu.intellij.ui.ZOnItemSelectable
import java.awt.ItemSelectable
import kotlin.reflect.KMutableProperty0

internal class CommitCompletionConfigListWithOverride(
  private val valueModel: CollectionListModel<CommitCompletionConfig>,
  private val overrideProperty: AtomicBooleanProperty,
  private val appValue: KMutableProperty0<List<CommitCompletionConfig>>,
  private val prjValue: KMutableProperty0<CommitCompletionConfigListOverride>,
  private val overrideUi: ItemSelectable
) : UiItem, ModifyTracker {
  private val binding: Disposable

  init {
    initialize()
    overrideProperty.set(prjValue.invoke().enabled)
    binding = ZOnItemSelectable(overrideUi) { onOverrideChange(it) }
  }

  private fun initialize() {
    onOverrideChange(prjValue.invoke().enabled)
  }

  private fun onOverrideChange(overridden: Boolean) {
    val values: List<CommitCompletionConfig> = if (overridden) {
      prjValue.invoke().values
    } else {
      appValue.invoke()
    }
    valueModel.replaceAll(values.map { it.copy() })
  }

  override fun apply() {
    val enabled = overrideProperty.get()
    if (enabled) {
      val newValue = CommitCompletionConfigListOverride(
        enabled,
        valueModel.toList().map { it.copy() }
      )
      prjValue.set(newValue)
    }
  }

  override fun isModified(): Boolean {
    return if (overrideUi.selectedObjects != null) {
      prjValue.invoke().values != valueModel.toList()
    } else {
      false
    }
  }

  override fun dispose() {
    Disposer.dispose(binding)
  }
}
