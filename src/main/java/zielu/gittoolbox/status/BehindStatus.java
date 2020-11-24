package zielu.gittoolbox.status;

import java.util.OptionalInt;

public class BehindStatus {
  private static final BehindStatus EMPTY = new BehindStatus();

  private final int count;
  private final Status status;
  private final Integer delta;

  private BehindStatus(int count, Status status, Integer delta) {
    this.count = count;
    this.status = status;
    this.delta = delta;
  }

  private BehindStatus() {
    this.count = 0;
    this.status = Status.SUCCESS;
    this.delta = null;
  }

  public static BehindStatus create(RevListCount behind) {
    return new BehindStatus(behind.value(), behind.status(), null);
  }

  public static BehindStatus create(RevListCount behind, int delta) {
    return new BehindStatus(behind.value(), behind.status(), delta);
  }

  public static BehindStatus create(int count, int delta) {
    return new BehindStatus(count, Status.SUCCESS, delta);
  }

  public static BehindStatus create(int count) {
    return new BehindStatus(count, Status.SUCCESS, null);
  }

  public static BehindStatus empty() {
    return EMPTY;
  }

  public int behind() {
    return count;
  }

  public Status status() {
    return status;
  }

  public OptionalInt delta() {
    return delta != null ? OptionalInt.of(delta) : OptionalInt.empty();
  }
}
