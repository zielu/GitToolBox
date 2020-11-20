package zielu.gittoolbox.ui.update

internal class DefaultUpdateProjectAction : AbstractUpdateProjectAction(
  ID,
  "Vcs.UpdateProject"
) {
  override fun isDefault(): Boolean = true

  companion object {
    const val ID = "idea.update.project.action"
  }
}
