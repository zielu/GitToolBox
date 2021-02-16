package zielu.gittoolbox.ui.config.v2.props

import com.intellij.openapi.Disposable

internal interface UiItem : Disposable {
  fun apply()

  override fun dispose() {
    // do nothing
  }
}
