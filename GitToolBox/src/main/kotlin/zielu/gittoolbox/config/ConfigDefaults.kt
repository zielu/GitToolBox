package zielu.gittoolbox.config

internal object ConfigDefaults {
    val decorationParts: List<DecorationPartConfig> = arrayListOf(
            DecorationPartConfig(DecorationPartType.LOCATION, "- "),
            DecorationPartConfig(DecorationPartType.BRANCH),
            DecorationPartConfig(DecorationPartType.STATUS),
            DecorationPartConfig(DecorationPartType.TAGS_ON_HEAD, "(", ")"),
            DecorationPartConfig(DecorationPartType.CHANGED_COUNT, "/ ")
    )
}
