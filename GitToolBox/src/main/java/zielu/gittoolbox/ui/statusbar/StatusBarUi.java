package zielu.gittoolbox.ui.statusbar;

public interface StatusBarUi {
  void setVisible(boolean visible);

  default void opened() {
  }

  default void closed() {
  }
}
