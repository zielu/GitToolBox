package zielu.gittoolbox.ui.update

internal class GitExtenderUpdateProjectAction : AbstractUpdateProjectAction(
  "gitextender.update.project.action",
  "GitExtender.UpdateAll"
) {
  override fun isDefault(): Boolean = false
}
