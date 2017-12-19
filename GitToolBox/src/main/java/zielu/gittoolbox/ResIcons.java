package zielu.gittoolbox;

import com.intellij.openapi.util.IconLoader;
import javax.swing.Icon;

public final class ResIcons {
  public static final Icon BranchOrange = IconLoader.getIcon("/zielu/gittoolbox/git-icon-orange.png");
  public static final Icon BranchViolet = IconLoader.getIcon("/zielu/gittoolbox/git-icon-violet.png");
  public static final Icon Warning = IconLoader.getIcon("/zielu/gittoolbox/exclamation-circle-frame.png");
  public static final Icon Error = IconLoader.getIcon("/zielu/gittoolbox/exclamation-red-frame.png");
  public static final Icon Ok = IconLoader.getIcon("/zielu/gittoolbox/tick-circle-frame.png");
  public static final Icon Edit = IconLoader.getIcon("/zielu/gittoolbox/edit.png");
  public static final Icon RegExp = IconLoader.getIcon("/zielu/gittoolbox/regular-expression-search.png");

  private ResIcons() {
    throw new IllegalStateException();
  }
}
