package zielu.gittoolbox.config

import com.intellij.openapi.project.Project

internal data class MutableConfig(
  val app: GitToolBoxConfig2,
  private val prj: GitToolBoxConfigPrj?,
  private val project: Project?
) {

  constructor(app: GitToolBoxConfig2) : this(app, null, null)

  fun hasProject(): Boolean = prj != null

  fun prj(): GitToolBoxConfigPrj {
    return prj!!
  }

  fun project(): Project {
    return project!!
  }

  fun copy(): MutableConfig {
    return MutableConfig(
      app.copy(),
      prj?.copy(),
      project
    )
  }
}
