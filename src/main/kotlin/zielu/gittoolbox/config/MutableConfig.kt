package zielu.gittoolbox.config

internal data class MutableConfig(val app: GitToolBoxConfig2, private val prj: GitToolBoxConfigPrj?) {

  constructor(app: GitToolBoxConfig2) : this(app, null)

  fun hasProject(): Boolean = prj != null

  fun prj(): GitToolBoxConfigPrj {
    return prj!!
  }
}
