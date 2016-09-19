package zielu.gittoolbox.push;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Collections;

public class GtPushResult {

    public enum Type {
        SUCCESS, REJECTED, ERROR, CANCELLED, NOT_AUTHORIZED
    }

    private final Type myType;
    private final String myOutput;
    private Collection<String> myBranches;


    private GtPushResult(Type type, String output) {
        myType = type;
        myOutput = output;
    }

    public Type getType() {
        return myType;
    }

    public String getOutput() {
        return myOutput;
    }

    public Collection<String> getRejectedBranches() {
        if (myBranches != null) {
            return myBranches;
        } else {
            return Collections.emptyList();
        }
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
        result.myBranches = ImmutableList.copyOf(rejectedBranches);
        return result;
    }
}
