package zielu.gittoolbox.push;

import com.intellij.openapi.util.Key;
import git4idea.commands.GitLineHandlerListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitPushRejectedDetector implements GitLineHandlerListener {

  private static final Pattern REJECTED_PATTERN = Pattern.compile("\\s+! \\[rejected\\]\\s+(\\S+) -> (\\S+) .*");

  private final Collection<RejectedRef> rejectedRefs = new ArrayList<RejectedRef>();

  @Override
  public void onLineAvailable(String line, Key outputType) {
    Matcher matcher = REJECTED_PATTERN.matcher(line);
    if (matcher.matches()) {
      rejectedRefs.add(createRejected(matcher));
    }
  }

  private RejectedRef createRejected(Matcher matcher) {
    String src = matcher.group(1);
    String dst = matcher.group(2);
    return new RejectedRef(src, dst);
  }

  @Override
  public void processTerminated(int exitCode) {
    //do nothing
  }

  @Override
  public void startFailed(Throwable exception) {
    //do nothing
  }

  public boolean rejected() {
    return !rejectedRefs.isEmpty();
  }

  public Collection<String> getRejectedBranches() {
    Collection<String> branches = new ArrayList<String>(rejectedRefs.size());
    for (RejectedRef rejectedRef : rejectedRefs) {
      branches.add(rejectedRef.source);
    }
    return branches;
  }

  static class RejectedRef {
    private final String source;
    private final String destination;

    RejectedRef(String source, String destination) {
      this.destination = destination;
      this.source = source;
    }
  }
}
