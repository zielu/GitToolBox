package zielu.gittoolbox.completion.gitmoji

import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import zielu.gittoolbox.UtfSeq
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

internal object GitmojiMetadata {
    private val metadata: Gitmojis by lazy {
        load()
    }
    private val characters: ConcurrentMap<String, List<Char>> = ConcurrentHashMap()

    private fun load(): Gitmojis {
        val yaml = Yaml(Constructor(Gitmojis::class.java))
        return yaml.load(this::class.java.getResourceAsStream("/zielu/gittoolbox/gitmoji.yaml"))
    }

    fun getKeywords(gitmoji: String): List<String> {
        return metadata.gitmojis[gitmoji]?.keywords ?: listOf()
    }

    fun getCharacters(gitmoji: String): List<Char> {
        return metadata.gitmojis[gitmoji]?.let { found ->
            characters.computeIfAbsent(gitmoji) {
                UtfSeq.fromCodepoint(found.codepoint, found.requiresVariation)
            }
        } ?: listOf()
    }
}

data class Gitmojis(
  var gitmojis: Map<String, Gitmoji> = mapOf()
)

data class Gitmoji(
  var keywords: List<String> = listOf(),
  var requiresVariation: Boolean = false,
  var codepoint: String = ""
)
