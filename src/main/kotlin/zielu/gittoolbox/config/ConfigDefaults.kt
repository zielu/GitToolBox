package zielu.gittoolbox.config

internal object ConfigDefaults {
  private val decorationParts: List<DecorationPartConfig> = listOf(
    DecorationPartConfig(DecorationPartType.LOCATION, "- "),
    DecorationPartConfig(DecorationPartType.BRANCH),
    DecorationPartConfig(DecorationPartType.STATUS),
    DecorationPartConfig(DecorationPartType.TAGS_ON_HEAD, "(", ")"),
    DecorationPartConfig(DecorationPartType.CHANGED_COUNT, "/ ")
  )

  fun decorationParts(): List<DecorationPartConfig> {
    return decorationParts.map { it.copy() }.toMutableList()
  }
}
