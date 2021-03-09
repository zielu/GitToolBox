package zielu.gittoolbox.ui.config.v2.props

import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.observable.properties.AtomicLazyProperty
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import zielu.gittoolbox.config.override.IntValueOverride
import zielu.intellij.test.MockItemSelectable

internal class IntPropWithOverrideTest {
  private val valueProperty = AtomicLazyProperty { 0 }
  private val overrideProperty = AtomicBooleanProperty(false)
  private var appValue = 0
  private val prjValue = IntValueOverride()
  private var valueUi = 0
  private val overrideUi = MockItemSelectable()

  private fun createProp(): IntPropWithOverride {
    return IntPropWithOverride(
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
    appValue = 1

    // when
    createProp()

    // then
    assertThat(valueProperty.get()).isEqualTo(appValue)
  }

  @Test
  fun `initial project value is propagated to value property`() {
    // given
    prjValue.enabled = true
    prjValue.value = 1

    // when
    createProp()

    // then
    assertThat(valueProperty.get()).isEqualTo(prjValue.value)
  }

  @Test
  fun `value is applied to project config when override is enabled`() {
    // given
    val prop = createProp()
    overrideProperty.set(true)
    valueProperty.set(1)

    // when
    prop.apply()

    // then
    assertAll(
      { assertThat(prjValue.enabled).isTrue },
      { assertThat(prjValue.value).isEqualTo(valueProperty.get()) }
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
      { assertThat(prjValue.value).isZero() }
    )
  }

  @Test
  fun `ui is refreshed when override is changed`() {
    // given
    val valueForPrj = 1
    prjValue.value = valueForPrj
    createProp()

    // when
    overrideUi.selectedObjects = arrayOf(1)
    overrideUi.fireSelected()

    // then
    assertThat(valueUi).isEqualTo(valueForPrj)

    // when
    overrideUi.selectedObjects = null
    overrideUi.fireSelected()

    // then
    assertThat(valueUi).isZero()
  }
}
