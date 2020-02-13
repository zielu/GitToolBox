package zielu.gittoolbox.completion.gitmoji

import org.yaml.snakeyaml.Yaml

internal object GitmojiKeywords {
    private val keywords: Map<String, List<String>> by lazy {
        load()
    }

    private fun load(): Map<String, List<String>> {
        return Yaml().load(this::class.java.getResourceAsStream("/zielu/gittoolbox/gitmoji.yaml"))
    }

    fun getKeywords(gitmoji: String): List<String> {
        return keywords.getOrDefault(gitmoji, listOf())
    }
}
