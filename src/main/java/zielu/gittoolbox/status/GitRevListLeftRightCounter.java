package zielu.gittoolbox.status;

import com.intellij.openapi.util.Key;
import com.intellij.vcs.log.Hash;
import git4idea.commands.GitLineHandlerListener;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.util.GtUtil;

public class GitRevListLeftRightCounter implements GitLineHandlerListener {
  private final AtomicInteger ahead = new AtomicInteger();
  private final AtomicReference<Hash> aheadHash = new AtomicReference<>();
  private final AtomicInteger behind = new AtomicInteger();
  private final AtomicReference<Hash> behindHash = new AtomicReference<>();

  private int exitCode;
  @Nullable
  private Throwable error;

  @Override
  public void onLineAvailable(String line, Key outputType) {
    if (aheadLine(line)) {
      onAheadLine(line);
    } else if (behindLine(line)) {
      onBehindLine(line);
    }
  }

  private void onAheadLine(String line) {
    ahead.incrementAndGet();
    if (aheadHash.get() == null) {
      aheadHash.set(hashFromLine(line));
    }
  }

  private void onBehindLine(String line) {
    behind.incrementAndGet();
    if (behindHash.get() == null) {
      behindHash.set(hashFromLine(line));
    }
  }

  private boolean aheadLine(String line) {
    return line.startsWith("<");
  }

  private boolean behindLine(String line) {
    return line.startsWith(">");
  }

  private Hash hashFromLine(String line) {
    return GtUtil.hash(line.substring(1));
  }

  public int ahead() {
    return ahead.get();
  }

  public int behind() {
    return behind.get();
  }

  @Nullable
  public Hash aheadTop() {
    return aheadHash.get();
  }

  @Nullable
  public Hash behindTop() {
    return behindHash.get();
  }

  public boolean isSuccess() {
    return exitCode == 0 && error == null;
  }

  @Override
  public void processTerminated(int exitCode) {
    this.exitCode = exitCode;
  }

  @Override
  public void startFailed(Throwable throwable) {
    error = throwable;
  }
}
