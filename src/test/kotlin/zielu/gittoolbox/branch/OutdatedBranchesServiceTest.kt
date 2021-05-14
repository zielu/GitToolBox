package zielu.gittoolbox.branch

import com.intellij.vcs.log.Hash
import git4idea.GitLocalBranch
import git4idea.GitRemoteBranch
import git4idea.branch.GitBranchesCollection
import git4idea.repo.GitRepository
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class OutdatedBranchesServiceTest {
  @MockK
  private lateinit var facadeMock: OutdatedBranchesFacade
  private lateinit var service: OutdatedBranchesService

  @BeforeEach
  fun beforeEach() {
    service = OutdatedBranchesService(facadeMock)
  }

  @Test
  fun `should calculate outdated branches`(
    @MockK repoMock: GitRepository,
    @MockK branchesCollectionMock: GitBranchesCollection,
    @MockK localNotMergedMock: GitLocalBranch,
    @MockK localMergedMock: GitLocalBranch,
    @MockK remoteNotMergedMock: GitRemoteBranch,
    @MockK remoteMergedMock: GitRemoteBranch,
    @MockK hashMock: Hash
  ) {
    // given
    val current = GitLocalBranch("master")
    val local = GitLocalBranch("only-local")

    every { localNotMergedMock.name } returns "local-not-merged"
    every { localMergedMock.name } returns "local-merged"
    every { repoMock.branches } returns branchesCollectionMock
    every { repoMock.currentBranch } returns current
    every { branchesCollectionMock.localBranches } returns listOf(local, localNotMergedMock, localMergedMock)
    every { facadeMock.findNotMergedBranches(repoMock) } returns setOf("local-not-merged")
    every { facadeMock.findMergedBranches(repoMock) } returns setOf("local-merged")
    every { facadeMock.getLatestCommitTimestamp(repoMock, any()) } returns null
    every { facadeMock.getExclusions() } returns OutdatedBranchesExclusions(listOf())
    every { facadeMock.getBranchHash(any(), any()) } returns hashMock
    every { local.findTrackedBranch(repoMock) } returns null
    every { localNotMergedMock.findTrackedBranch(repoMock) } returns remoteNotMergedMock
    every { localMergedMock.findTrackedBranch(repoMock) } returns remoteMergedMock
    every { hashMock.asString() } returns "hash"

    // when
    val outdatedBranches = service.outdatedBranches(repoMock)

    // then
    assertThat(outdatedBranches).extracting("localBranch").containsExactlyInAnyOrder(local, localMergedMock)
  }
}
