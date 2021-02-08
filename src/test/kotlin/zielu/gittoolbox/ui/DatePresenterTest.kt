package zielu.gittoolbox.ui

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.extension.ExtendWith
import zielu.gittoolbox.config.AbsoluteDateTimeStyle
import zielu.gittoolbox.config.DateType
import java.time.Clock
import java.time.ZonedDateTime

@ExtendWith(MockKExtension::class)
internal class DatePresenterTest {
  @MockK
  private lateinit var mockFacade: DatePresenterFacade
  private lateinit var datePresenter: DatePresenter

  @BeforeEach
  fun beforeEach() {
    datePresenter = DatePresenterImpl(mockFacade)
    every { mockFacade.getClock() } returns Clock.systemDefaultZone()
    every { mockFacade.getAbsoluteDateTimeFormat() } returns AbsoluteDateTimeStyle.FROM_LOCALE.format
  }

  @Test
  fun `format should return expected value if hidden`() {
    // when
    val formatted = datePresenter.format(DateType.HIDDEN, ZonedDateTime.now())

    // then
    assertThat(formatted).isEmpty()
  }

  @TestFactory
  fun `format should return not empty value`(): List<DynamicNode> {
    val types = DateType.values().toMutableList()
    types.remove(DateType.HIDDEN)

    return types.map { type ->
      DynamicTest.dynamicTest("should return not empty value for $type") {
        // when
        val formatted = datePresenter.format(type, ZonedDateTime.now())

        // then
        assertThat(formatted).isNotBlank()
      }
    }
  }
}
