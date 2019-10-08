package zielu.gittoolbox.fetch

import git4idea.repo.GitRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import zielu.TestType
import zielu.gittoolbox.config.AutoFetchExclusionConfig

@Tag(TestType.FAST)
@ExtendWith(MockitoExtension::class)
internal class RemoteFilteredRepositoryTest {

  @Test
  internal fun sameDelegatesAreEqual(@Mock delegate: GitRepository) {
    val config = AutoFetchExclusionConfig("")
    val wrapper1 = RemoteFilteredRepository(delegate, config)
    val wrapper2 = RemoteFilteredRepository(delegate, config)

    assertThat(wrapper1).isEqualTo(wrapper2)
  }
}
