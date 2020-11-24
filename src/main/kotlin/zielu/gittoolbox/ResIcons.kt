package zielu.gittoolbox

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

internal object ResIcons {
  @JvmStatic
  val BranchOrange: Icon
    get() = IconLoader.getIcon("/zielu/gittoolbox/git-icon-orange.png")
  @JvmStatic
  val BranchViolet: Icon
    get() = IconLoader.getIcon("/zielu/gittoolbox/git-icon-violet.png")
  @JvmStatic
  val Warning: Icon
    get() = IconLoader.getIcon("/zielu/gittoolbox/exclamation-circle-frame.png")
  @JvmStatic
  val Error: Icon
    get() = IconLoader.getIcon("/zielu/gittoolbox/exclamation-red-frame.png")
  @JvmStatic
  val Ok: Icon
    get() = IconLoader.getIcon("/zielu/gittoolbox/tick-circle-frame.png")
  @JvmStatic
  val Edit: Icon
    get() = IconLoader.getIcon("/zielu/gittoolbox/edit.png")
  @JvmStatic
  val RegExp: Icon
    get() = IconLoader.getIcon("/zielu/gittoolbox/regular-expression-search.png")
  @JvmStatic
  val Commit: Icon
    get() = IconLoader.getIcon("/zielu/gittoolbox/commit.png")
  @JvmStatic
  val Blame: Icon
    get() = IconLoader.getIcon("/zielu/gittoolbox/git-icon-black.png")
  @JvmStatic
  val ChangesPresent: Icon
    get() = IconLoader.getIcon("/zielu/gittoolbox/changes-present.svg")
  @JvmStatic
  val NoChanges: Icon
    get() = IconLoader.getIcon("/zielu/gittoolbox/changes-none.svg")
  @JvmStatic
  val Logo: Icon
    get() = IconLoader.getIcon("/META-INF/pluginIcon.svg")
}
