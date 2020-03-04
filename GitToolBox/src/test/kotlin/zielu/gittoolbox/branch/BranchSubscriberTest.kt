package zielu.gittoolbox.branch

import com.intellij.vcs.log.Hash
import git4idea.GitLocalBranch
import git4idea.repo.GitRepository
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import zielu.gittoolbox.cache.RepoInfo
import zielu.gittoolbox.cache.RepoStatus
import zielu.gittoolbox.cache.RepoStatusRemote

@ExtendWith(MockKExtension::class)
internal class BranchSubscriberTest {
  @RelaxedMockK
  private lateinit var gatewayMock: BranchSubscriberLocalGateway

  private lateinit var subscriber: BranchSubscriber

  @BeforeEach
  fun beforeEach() {
    subscriber = BranchSubscriber(gatewayMock)
  }

  @Test
  fun shouldDetectSwitchBetweenBranches(
    @MockK repositoryMock: GitRepository
  ) {
    // given
    val oldBranch = GitLocalBranch("old_branch")
    val newBranch = GitLocalBranch("new_branch")

    val oldRepoInfo = RepoInfo.create(
      RepoStatus.create(oldBranch, null, RepoStatusRemote.empty()),
      null,
      listOf()
    )
    val newRepoInfo = RepoInfo.create(
      RepoStatus.create(newBranch, null, RepoStatusRemote.empty()),
      null,
      listOf()
    )

    // when
    subscriber.onRepoStateChanged(oldRepoInfo, newRepoInfo, repositoryMock)

    // then
    verify {
      gatewayMock.branchSwitch(oldBranch, newBranch, repositoryMock)
    }
  }

  @Test
  fun shouldDetectSwitchBetweenDetachedAndBranch(
    @MockK repositoryMock: GitRepository,
    @MockK oldHash: Hash
  ) {
    // given
    val newBranch = GitLocalBranch("new_branch")

    every { oldHash.toShortString() } returns "old-hash"
    val oldRepoInfo = RepoInfo.create(
      RepoStatus.create(null, oldHash, RepoStatusRemote.empty()),
      null,
      listOf()
    )
    val newRepoInfo = RepoInfo.create(
      RepoStatus.create(newBranch, null, RepoStatusRemote.empty()),
      null,
      listOf()
    )

    // when
    subscriber.onRepoStateChanged(oldRepoInfo, newRepoInfo, repositoryMock)

    // then
    verify {
      gatewayMock.switchToBranchFromOther(newBranch, repositoryMock)
    }
  }

  @Test
  fun shouldDetectSwitchBranchAndDetached(
    @MockK repositoryMock: GitRepository,
    @MockK newHash: Hash
  ) {
    // given
    val oldBranch = GitLocalBranch("old_branch")

    val oldRepoInfo = RepoInfo.create(
      RepoStatus.create(oldBranch, null, RepoStatusRemote.empty()),
      null,
      listOf()
    )

    every { newHash.toShortString() } returns "new-hash"
    val newRepoInfo = RepoInfo.create(
      RepoStatus.create(null, newHash, RepoStatusRemote.empty()),
      null,
      listOf()
    )

    // when
    subscriber.onRepoStateChanged(oldRepoInfo, newRepoInfo, repositoryMock)

    // then
    verify {
      gatewayMock.switchFromBranchToOther(oldBranch, repositoryMock)
    }
  }
}
