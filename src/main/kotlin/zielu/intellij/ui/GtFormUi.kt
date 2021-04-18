package zielu.intellij.ui

import com.intellij.openapi.Disposable
import javax.swing.JComponent

internal interface GtFormUi : Disposable {
  val content: JComponent

  fun init()

  fun afterStateSet()

  override fun dispose() {
    // do nothing
  }
}
