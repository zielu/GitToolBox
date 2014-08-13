package zielu.gittoolbox.status;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

public class RevListCount {
    public static enum Status {
        Success,
        Cancel,
        Failure,
        NoRemote
    }

    private static final RevListCount cancel = new RevListCount(Status.Cancel, Optional.<Integer>absent());
    private static final RevListCount failure = new RevListCount(Status.Failure, Optional.<Integer>absent());
    private static final RevListCount noRemote = new RevListCount(Status.NoRemote, Optional.<Integer>absent());

    private final Optional<Integer> myValue;
    private final Status myStatus;

    private RevListCount(Status status, Optional<Integer> value){
        myValue = value;
        myStatus = status;
    }

    public int value() {
        Preconditions.checkState(Status.Success == myStatus, "Value not possible for {0}", myStatus);
        return myValue.get();
    }

    public Status status() {
        return myStatus;
    }

    public static RevListCount success(int count) {
        return new RevListCount(Status.Success, Optional.of(count));
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
}
