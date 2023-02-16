package zielu.gittoolbox.fetch

import com.intellij.mock.MockVirtualFile
import com.intellij.openapi.vfs.VirtualFile
import git4idea.repo.GitRepository
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import zielu.gittoolbox.config.AutoFetchExclusionConfig
import zielu.gittoolbox.config.RemoteConfig
import zielu.intellij.test.createRemote

@ExtendWith(MockKExtension::class)
internal class AutoFetchExclusionsTest {
  private val repoRoot: VirtualFile = MockVirtualFile("repo-root")
  @MockK
  private lateinit var repository: GitRepository

  @BeforeEach
  fun beforeEach() {
    every { repository.root } returns repoRoot
  }

  @Test
  fun `should exclude repo if it is configured and has no remotes`() {
    // given
    val exclusionsMap: Map<String, AutoFetchExclusionConfig> = mapOf(
      repoRoot.url to AutoFetchExclusionConfig(repoRoot.url)
    )
    val exclusions = AutoFetchExclusions { exclusionsMap }

    // when
    val result = exclusions.apply(listOf(repository))

    // then
    assertThat(result).isEmpty()
  }

  @Test
  fun `should not exclude repo if it is not configured`() {
    // given
    val exclusions = AutoFetchExclusions { mapOf<String, AutoFetchExclusionConfig>() }

    // when
    val result = exclusions.apply(listOf(repository))

    // then
    assertThat(result).containsOnly(repository)
  }

  @Test
  fun `should exclude remote if it is configured`() {
    // given
    val upstream = createRemote("upstream")
    val origin = createRemote("origin")
    every { repository.remotes } returns listOf(upstream, origin)
    val exclusionsMap: Map<String, AutoFetchExclusionConfig> = mapOf(
      repoRoot.url to AutoFetchExclusionConfig(repoRoot.url, mutableListOf(RemoteConfig(upstream.name)))
    )
    val exclusions = AutoFetchExclusions { exclusionsMap }

    // when
    val result = exclusions.apply(listOf(repository))
    assertSoftly { softly ->
      softly.assertThat(result).hasSize(1)
      softly.assertThat(result[0].remotes).containsOnly(origin)
    }
  }
}
