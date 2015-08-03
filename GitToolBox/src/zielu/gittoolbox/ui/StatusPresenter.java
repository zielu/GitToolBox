package zielu.gittoolbox.ui;

public interface StatusPresenter {
    String behindStatus(int behind);
    String aheadBehindStatus(int ahead, int behind);
    String nonZeroAheadBehindStatus(int ahead, int behind);
    String key();
    String getLabel();
}
