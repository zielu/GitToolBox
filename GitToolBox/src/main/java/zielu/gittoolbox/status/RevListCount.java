package zielu.gittoolbox.status;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.intellij.vcs.log.Hash;
import org.jetbrains.annotations.Nullable;


public class RevListCount {

    private static final RevListCount cancel = new RevListCount(Status.Cancel, null, null);
    private static final RevListCount failure = new RevListCount(Status.Failure, null, null);
    private static final RevListCount noRemote = new RevListCount(Status.NoRemote, null, null);

    private final Integer myValue;
    private final Hash myTop;
    private final Status myStatus;

    private RevListCount(Status status, @Nullable Integer value, @Nullable Hash top) {
        myValue = value;
        myStatus = status;
        myTop = top;
    }

    public int value() {
        Preconditions.checkState(Status.Success == myStatus, "Value not possible for {0}", myStatus);
        return myValue;
    }

    @Nullable
    public Hash top() {
        return myTop;
    }

    public Status status() {
        return myStatus;
    }

    public static RevListCount success(int count, Hash top) {
        return new RevListCount(Status.Success, count, top);
    }

    public static RevListCount cancel() {
        return cancel;
    }

    public static RevListCount failure() {
        return failure;
    }

    public static RevListCount noRemote() {
        return noRemote;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RevListCount that = (RevListCount) o;
        return Objects.equal(myValue, that.myValue) &&
                Objects.equal(myTop, that.myTop) &&
            myStatus != that.myStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(myValue, myStatus);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("value", myValue)
            .add("status", myStatus)
            .add("top", myTop)
            .toString();
    }
}
