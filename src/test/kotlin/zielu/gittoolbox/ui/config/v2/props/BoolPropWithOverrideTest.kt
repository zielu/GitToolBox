package zielu.gittoolbox.ui.config.v2.props

import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import zielu.gittoolbox.config.override.BoolValueOverride
import zielu.intellij.test.MockItemSelectable

internal class BoolPropWithOverrideTest {
  private val valueProperty = AtomicBooleanProperty(false)
  private val overrideProperty = AtomicBooleanProperty(false)
  private var appValue = false
  private val prjValue = BoolValueOverride()
  private var valueUi = false
  private val overrideUi = MockItemSelectable()

  private fun createProp(): BoolPropWithOverride {
    return BoolPropWithOverride(
      valueProperty,
      overrideProperty,
      this::appValue,
      prjValue,
      { valueUi = it },
      overrideUi
    )
  }

  @Test
  fun `initial app value is propagated to value property`() {
    // given
    appValue = true

    // when
    createProp()

    // then
    assertThat(valueProperty.get()).isTrue
  }

  @Test
  fun `initial project value is propagated to value property`() {
    // given
    prjValue.enabled = true
    prjValue.value = true

    // when
    createProp()

    // then
    assertThat(valueProperty.get()).isTrue
  }

  @Test
  fun `value is applied to project config when override is enabled`() {
    // given
    val prop = createProp()
    overrideProperty.set(true)
    valueProperty.set(true)

    // when
    prop.apply()

    // then
    assertAll(
      { assertThat(prjValue.enabled).isTrue },
      { assertThat(prjValue.value).isTrue }
    )
  }

  @Test
  fun `value is not applied to project config when override is enabled`() {
    // given
    val prop = createProp()

    // when
    prop.apply()

    // then
    assertAll(
      { assertThat(prjValue.enabled).isFalse },
      { assertThat(prjValue.value).isFalse }
    )
  }

  @Test
  fun `ui is refreshed when override is changed`() {
    // given
    prjValue.value = true
    createProp()

    // when
    overrideUi.selectedObjects = arrayOf(1)
    overrideUi.fireSelected()

    // then
    assertThat(valueUi).isTrue

    // when
    overrideUi.selectedObjects = null
    overrideUi.fireSelected()

    // then
    assertThat(valueUi).isFalse
  }
}
