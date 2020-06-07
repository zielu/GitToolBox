package zielu.gittoolbox.completion.gitmoji

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class GitmojiMetadataTest {

    @Test
    fun shouldReturnKeywords() {
        val keywords = GitmojiMetadata.getKeywords("alembic")

        assertThat(keywords).isNotEmpty
    }

    @Test
    fun shouldReturnCharacters() {
        val characters = GitmojiMetadata.getCharacters("alembic")

        assertThat(characters).isNotEmpty
    }
}
