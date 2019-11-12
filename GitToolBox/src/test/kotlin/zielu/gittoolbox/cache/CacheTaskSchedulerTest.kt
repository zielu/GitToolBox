package zielu.gittoolbox.cache

import git4idea.repo.GitRepository
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import zielu.TestType
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertTrue

@Tag(TestType.FAST)
@ExtendWith(MockKExtension::class)
internal class CacheTaskSchedulerTest {
  @RelaxedMockK
  private lateinit var gatewayMock: CacheTaskSchedulerLocalGateway
  private lateinit var scheduler: CacheTaskScheduler

  @BeforeEach
  fun beforeEach() {
    scheduler = CacheTaskScheduler(gatewayMock)
    scheduler.setTaskDelayMillis(30)
  }

  @AfterEach
  fun afterEach() {
    scheduler.dispose()
  }

  @Test
  @Throws(InterruptedException::class)
  fun scheduleOptionalShouldDiscardSecondTask(@MockK repository: GitRepository) {
    // given
    val executedCount = CountDownLatch(1)
    val discardedCount = CountDownLatch(1)

    // when
    scheduler.scheduleOptional(repository, DummyTask(executedCount))
    scheduler.scheduleOptional(repository, DummyTask(executedCount, discardedCount))

    // then
    assertSoftly { softly ->
      softly.assertThat(discardedCount.await(1, TimeUnit.SECONDS)).isTrue
      softly.assertThat(executedCount.await(1, TimeUnit.SECONDS)).isTrue
    }
  }

  @Test
  @Throws(InterruptedException::class)
  fun scheduleMandatoryShouldExecuteTwoTasks(@MockK repository: GitRepository) {
    // given
    val executedCount = CountDownLatch(2)

    // when
    scheduler.scheduleMandatory(repository, DummyTask(executedCount))
    scheduler.scheduleMandatory(repository, DummyTask(executedCount))

    // then
    assertTrue { executedCount.await(1, TimeUnit.SECONDS) }
  }

  @Test
  fun canRemoveRepositoryIfNeverScheduled(@MockK repository: GitRepository) {
    scheduler.removeRepository(repository)
  }

  private class DummyTask(
    private val runLatch: CountDownLatch,
    private val discardedLatch: CountDownLatch = CountDownLatch(0)
  ) : CacheTaskScheduler.Task {
    override fun run(repository: GitRepository) = runLatch.countDown()
    override fun discarded() = discardedLatch.countDown()
  }
}
