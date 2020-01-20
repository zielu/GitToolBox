package zielu.gittoolbox.config.override

import com.intellij.openapi.project.Project

internal data class BoolConfigOverride(
  var enabled: Boolean = false,
  var value: Boolean = false,
  var applied: MutableList<AppliedConfigOverride> = arrayListOf()
) {

  fun copy(): BoolConfigOverride {
    return BoolConfigOverride(
      enabled,
      value,
      applied.map { it.copy() }.toMutableList()
    )
  }

  fun isNotApplied(project: Project): Boolean {
      return project.projectFilePath?.let { path ->
        applied.map { it.projectPath }.any { it == path }
      } ?: false
  }

  fun applied(project: Project) {
    project.projectFilePath?.run {
      applied.add(AppliedConfigOverride(this))
    }
  }
}
