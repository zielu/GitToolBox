package zielu.gittoolbox.fetch

import git4idea.repo.GitRepository
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import zielu.TestType
import zielu.gittoolbox.config.AutoFetchExclusionConfig
import zielu.gittoolbox.config.RemoteConfig
import zielu.intellij.test.createRemote

@Tag(TestType.FAST)
@ExtendWith(MockKExtension::class)
internal class RemoteFilteredRepositoryTest {

  @Test
  fun sameDelegatesAreEqual(@MockK delegate: GitRepository) {
    // given
    val config = AutoFetchExclusionConfig("")
    val wrapper1 = RemoteFilteredRepository(delegate, config)
    val wrapper2 = RemoteFilteredRepository(delegate, config)

    // then
    assertThat(wrapper1).isEqualTo(wrapper2)
  }

  @Test
  fun getRemotesAppliesFiltering(@MockK delegate: GitRepository) {
    // given
    val keptRemote = createRemote("origin")
    val filteredOutRemoteName = "upstream"
    every { delegate.remotes } returns listOf(keptRemote, createRemote(filteredOutRemoteName))
    val config = AutoFetchExclusionConfig("")
    config.excludedRemotes.add(RemoteConfig(filteredOutRemoteName))
    val wrapper = RemoteFilteredRepository(delegate, config)

    // when
    val filteredRemotes = wrapper.remotes

    // then
    assertThat(filteredRemotes).containsOnly(keptRemote)
  }
}
