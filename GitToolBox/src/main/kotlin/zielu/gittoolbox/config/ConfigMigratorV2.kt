package zielu.gittoolbox.config

internal object ConfigMigratorV2 {
  fun migrate1To2(config: GitToolBoxConfig2) {
    if (!config.hideInlineBlameWhileDebugging) {
      config.alwaysShowInlineBlameWhileDebugging = true
    }
  }

  fun migrate2To3(config: GitToolBoxConfig2) {
    val broken = config.decorationParts.any { it.type == DecorationPartType.UNKNOWN }
    if (broken) {
      config.decorationParts = ConfigDefaults.decorationParts
    }
  }
}
