package zielu.gittoolbox.cache

import git4idea.repo.GitRepository
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.concurrent.CountDownLatch

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
  fun `scheduleOptional should discard second task`(@MockK repository: GitRepository) {
    // given
    val executedCount = CountDownLatch(1)
    val discardedCount = CountDownLatch(1)

    // when
    scheduler.scheduleOptional(repository, DummyTask(executedCount))
    scheduler.scheduleOptional(repository, DummyTask(executedCount, discardedCount))

    // then
    verify(exactly = 1) {
      gatewayMock.schedule(any(), any())
    }
  }

  @Test
  @Throws(InterruptedException::class)
  fun `scheduleMandatory should execute two tasks`(@MockK repository: GitRepository) {
    // given
    val executedCount = CountDownLatch(2)

    // when
    scheduler.scheduleMandatory(repository, DummyTask(executedCount))
    scheduler.scheduleMandatory(repository, DummyTask(executedCount))

    // then
    verify(exactly = 2) {
      gatewayMock.schedule(any(), any())
    }
  }

  @Test
  fun `should remove repository if tasks were never scheduled`(@MockK repository: GitRepository) {
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
