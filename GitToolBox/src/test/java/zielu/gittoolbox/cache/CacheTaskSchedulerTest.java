package zielu.gittoolbox.cache;

import static org.assertj.core.api.Assertions.assertThat;

import com.intellij.openapi.project.Project;
import git4idea.repo.GitRepository;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import zielu.gittoolbox.metrics.MockMetrics;

@Tag("fast")
@ExtendWith({MockitoExtension.class})
class CacheTaskSchedulerTest {
  @Mock(stubOnly = true)
  private GitRepository repository;
  @Mock(stubOnly = true)
  private Project project;

  private CacheTaskScheduler scheduler;

  @BeforeEach
  void before() {
    scheduler = new CacheTaskScheduler(project, new MockMetrics());
    scheduler.setTaskDelayMillis(30);
    scheduler.initialize();
    scheduler.opened();
  }

  @AfterEach
  void after() {
    scheduler.closed();
    scheduler.dispose();
  }

  @Test
  void scheduleOptional() throws InterruptedException {
    CountDownLatch executedCount = new CountDownLatch(1);
    CountDownLatch discardedCount = new CountDownLatch(1);
    scheduler.scheduleOptional(repository, new DummyTask(executedCount));
    scheduler.scheduleOptional(repository, new DummyTask(executedCount, discardedCount));

    assertThat(discardedCount.await(1, TimeUnit.SECONDS)).isTrue();
    assertThat(executedCount.await(1, TimeUnit.SECONDS)).isTrue();
  }

  @Test
  void scheduleMandatory() throws InterruptedException {
    CountDownLatch executedCount = new CountDownLatch(2);
    scheduler.scheduleMandatory(repository, new DummyTask(executedCount));
    scheduler.scheduleMandatory(repository, new DummyTask(executedCount));

    assertThat(executedCount.await(1, TimeUnit.SECONDS)).isTrue();
  }

  @Test
  void removeRepositoryIfNeverScheduled() {
    scheduler.removeRepository(repository);
  }

  private static final class DummyTask implements CacheTaskScheduler.Task {
    private final CountDownLatch runLatch;
    private final CountDownLatch discardedLatch;

    private DummyTask(CountDownLatch runLatch, CountDownLatch discardedLatch) {
      this.runLatch = runLatch;
      this.discardedLatch = discardedLatch;
    }

    private DummyTask(CountDownLatch runLatch) {
      this(runLatch, new CountDownLatch(0));
    }

    @Override
    public void run(@NotNull GitRepository repository) {
      runLatch.countDown();
    }

    @Override
    public void discarded(@NotNull GitRepository repository) {
      discardedLatch.countDown();
    }
  }
}