package zielu.gittoolbox.status;

public enum Status {
    Success(true),
    NoRemote(true),
    Cancel(false),
    Failure(false)
    ;

    private final boolean valid;

    Status(boolean _valid) {
        valid = _valid;
    }

    public boolean isValid() {
        return valid;
    }
}
