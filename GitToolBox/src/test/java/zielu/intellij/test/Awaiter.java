package zielu.intellij.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class Awaiter {
  private final Semaphore semaphore = new Semaphore(0);

  public void satisfied() {
    semaphore.release();
  }

  public void await() {
    try {
      assertThat(semaphore.tryAcquire(30, TimeUnit.SECONDS)).isTrue();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
