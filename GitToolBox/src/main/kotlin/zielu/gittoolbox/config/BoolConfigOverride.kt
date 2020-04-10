package zielu.gittoolbox.config

import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.annotations.Transient

internal data class BoolConfigOverride(
  var enabled: Boolean = false,
  var value: Boolean = false,
  var applied: MutableList<AppliedConfigOverride> = arrayListOf()
) {

  @Transient
  fun copy(): BoolConfigOverride {
    return BoolConfigOverride(
      enabled,
      value,
      applied.map { it.copy() }.toMutableList()
    )
  }

  fun isNotApplied(project: Project): Boolean {
      return project.presentableUrl?.let { path ->
        applied.asSequence()
          .map { it.projectPath }
          .none { it == path }
      } ?: false
  }

  fun applied(project: Project) {
    project.presentableUrl?.run {
      applied.add(AppliedConfigOverride(this))
    }
  }

  @Transient
  fun getAppliedPaths(): List<String> {
    return applied.asSequence()
      .map { it.projectPath }
      .toList()
  }
}
