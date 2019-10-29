package zielu.intellij.ui

import java.util.function.BiConsumer
import java.util.function.Function

internal class ConfigUiBinder<CONFIG, UI> {
  private val bindings: MutableList<Binding<CONFIG, UI, *>> = ArrayList()

  fun <T> bind(
    getConfig: Function<CONFIG, T>,
    setConfig: BiConsumer<CONFIG, T>,
    getUi: Function<UI, T>,
    setUi: BiConsumer<UI, T>
  ) {
    bindings.add(Binding(getConfig, setConfig, getUi, setUi))
  }

  fun populateUi(config: CONFIG, ui: UI) {
    bindings.forEach { it.populateUi(config, ui) }
  }

  fun checkModified(config: CONFIG, ui: UI): Boolean {
    return bindings.any { it.checkModified(config, ui) }
  }

  fun populateConfig(config: CONFIG, ui: UI) {
    bindings.forEach { it.populateConfig(config, ui) }
  }
}

private class Binding<CONFIG, UI, T>(
  val getConfig: Function<CONFIG, T>,
  val setConfig: BiConsumer<CONFIG, T>,
  val getUi: Function<UI, T>,
  val setUi: BiConsumer<UI, T>
) {

  fun populateUi(config: CONFIG, ui: UI) {
    setUi.accept(ui, getConfig.apply(config))
  }

  fun checkModified(config: CONFIG, ui: UI): Boolean {
    return getUi.apply(ui) != getConfig.apply(config)
  }

  fun populateConfig(config: CONFIG, ui: UI) {
    setConfig.accept(config, getUi.apply(ui))
  }
}
