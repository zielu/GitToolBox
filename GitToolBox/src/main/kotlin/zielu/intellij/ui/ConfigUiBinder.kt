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
    bindings.add(JavaBinding(getConfig, setConfig, getUi, setUi))
  }

  fun <T> bind(
    getFromConfig: Function<CONFIG, T>,
    setInUi: BiConsumer<UI, T>
  ) {
    bindings.add(JavaToUiBinding(getFromConfig, setInUi))
  }

  fun <T> bind(
    getFromConfig: (CONFIG) -> T,
    setInConfig: (CONFIG, T) -> Unit,
    getFromUi: (UI) -> T,
    setInUi: (UI, T) -> Unit
  ) {
    bindings.add(KtBinding(getFromConfig, setInConfig, getFromUi, setInUi))
  }

  fun <T> bind(
    getFromConfig: (CONFIG) -> T,
    setInUi: (UI, T) -> Unit
  ) {
    bindings.add(KtToUiBinding(getFromConfig, setInUi))
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

private interface Binding<CONFIG, UI, T> {
  fun populateUi(config: CONFIG, ui: UI)
  fun checkModified(config: CONFIG, ui: UI): Boolean
  fun populateConfig(config: CONFIG, ui: UI)
}

private class JavaBinding<CONFIG, UI, T>(
  val getConfig: Function<CONFIG, T>,
  val setConfig: BiConsumer<CONFIG, T>,
  val getUi: Function<UI, T>,
  val setUi: BiConsumer<UI, T>
) : Binding<CONFIG, UI, T> {
  override fun populateUi(config: CONFIG, ui: UI) {
    setUi.accept(ui, getConfig.apply(config))
  }

  override fun checkModified(config: CONFIG, ui: UI): Boolean {
    return getUi.apply(ui) != getConfig.apply(config)
  }

  override fun populateConfig(config: CONFIG, ui: UI) {
    setConfig.accept(config, getUi.apply(ui))
  }
}

private class JavaToUiBinding<CONFIG, UI, T>(
  val fromConfig: Function<CONFIG, T>,
  val toUi: BiConsumer<UI, T>
) : Binding<CONFIG, UI, T> {
  override fun populateUi(config: CONFIG, ui: UI) {
    toUi.accept(ui, fromConfig.apply(config))
  }

  override fun checkModified(config: CONFIG, ui: UI): Boolean = false

  override fun populateConfig(config: CONFIG, ui: UI) {
    // do nothing
  }
}

private class KtBinding<CONFIG, UI, T>(
  val fromConfig: (CONFIG) -> T,
  val toConfig: (CONFIG, T) -> Unit,
  val fromUi: (UI) -> T,
  val toUi: (UI, T) -> Unit
) : Binding<CONFIG, UI, T> {
  override fun populateUi(config: CONFIG, ui: UI) {
    toUi.invoke(ui, fromConfig.invoke(config))
  }

  override fun checkModified(config: CONFIG, ui: UI): Boolean {
    return fromUi.invoke(ui) != fromConfig.invoke(config)
  }

  override fun populateConfig(config: CONFIG, ui: UI) {
    toConfig.invoke(config, fromUi.invoke(ui))
  }
}

private class KtToUiBinding<CONFIG, UI, T>(
  val fromConfig: (CONFIG) -> T,
  val toUi: (UI, T) -> Unit
) : Binding<CONFIG, UI, T> {
  override fun populateUi(config: CONFIG, ui: UI) {
    toUi.invoke(ui, fromConfig.invoke(config))
  }

  override fun checkModified(config: CONFIG, ui: UI): Boolean = false

  override fun populateConfig(config: CONFIG, ui: UI) {
    // do nothing
  }
}
