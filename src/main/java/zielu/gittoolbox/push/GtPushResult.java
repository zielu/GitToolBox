package zielu.gittoolbox.push;

import com.google.common.collect.ImmutableList;
import java.util.Collection;

public class GtPushResult {

  private final Type type;
  private final String output;
  private Collection<String> branches = ImmutableList.of();

  private GtPushResult(Type type, String output) {
    this.type = type;
    this.output = output;
  }

  public static GtPushResult success() {
    return new GtPushResult(Type.SUCCESS, "");
  }

  public static GtPushResult error(String output) {
    return new GtPushResult(Type.ERROR, output);
  }

  public static GtPushResult cancel() {
    return new GtPushResult(Type.CANCELLED, "");
  }

  public static GtPushResult reject(Collection<String> rejectedBranches) {
    GtPushResult result = new GtPushResult(Type.REJECTED, "");
    result.branches = ImmutableList.copyOf(rejectedBranches);
    return result;
  }

  public Type getType() {
    return type;
  }

  public String getOutput() {
    return output;
  }

  public Collection<String> getRejectedBranches() {
    return branches;
  }

  public enum Type {
    SUCCESS, REJECTED, ERROR, CANCELLED, NOT_AUTHORIZED
  }
}
