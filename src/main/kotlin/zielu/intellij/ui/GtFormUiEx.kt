package zielu.intellij.ui

internal interface GtFormUiEx<T> : GtFormUi {
  fun fillFromState(state: T)
  fun isModified(): Boolean
  fun applyToState(state: T)
}
