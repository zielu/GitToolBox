package zielu.gittoolbox.config

internal data class MutableConfig(val app: GitToolBoxConfig2, val prjConfig: GitToolBoxConfigPrj?) {

  constructor(app: GitToolBoxConfig2) : this(app, null)

  fun hasProject(): Boolean = prjConfig != null
}
