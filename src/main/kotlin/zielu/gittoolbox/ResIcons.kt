package zielu.gittoolbox

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

internal object ResIcons {
  @JvmStatic
  val BranchOrange: Icon
    get() = getIcon("/zielu/gittoolbox/git-icon-orange.png")
  @JvmStatic
  val BranchViolet: Icon
    get() = getIcon("/zielu/gittoolbox/git-icon-violet.png")
  @JvmStatic
  val Warning: Icon
    get() = getIcon("/zielu/gittoolbox/exclamation-circle-frame.png")
  @JvmStatic
  val Error: Icon
    get() = getIcon("/zielu/gittoolbox/exclamation-red-frame.png")
  @JvmStatic
  val Ok: Icon
    get() = getIcon("/zielu/gittoolbox/tick-circle-frame.png")
  @JvmStatic
  val Edit: Icon
    get() = getIcon("/zielu/gittoolbox/edit.png")
  @JvmStatic
  val RegExp: Icon
    get() = getIcon("/zielu/gittoolbox/regular-expression-search.png")
  @JvmStatic
  val Commit: Icon
    get() = getIcon("/zielu/gittoolbox/commit.png")
  @JvmStatic
  val Blame: Icon
    get() = getIcon("/zielu/gittoolbox/git-icon-black.png")
  @JvmStatic
  val ChangesPresent: Icon
    get() = getIcon("/zielu/gittoolbox/changes-present.svg")
  @JvmStatic
  val NoChanges: Icon
    get() = getIcon("/zielu/gittoolbox/changes-none.svg")
  @JvmStatic
  val Logo: Icon
    get() = IconLoader.getIcon("/META-INF/pluginIcon.svg")
  @JvmStatic
  val Plus: Icon
    get() = IconLoader.getIcon("/zielu/gittoolbox/plus-circle.png")
  @JvmStatic
  val Minus: Icon
    get() = IconLoader.getIcon("/zielu/gittoolbox/minus-circle.png")

  private fun getIcon(path: String) = IconLoader.getIcon(path, javaClass)
}
