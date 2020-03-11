package zielu.gittoolbox.branch

import git4idea.GitLocalBranch
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import zielu.gittoolbox.repo.GtRepository
import zielu.gittoolbox.store.RecentBranch
import java.time.Instant

@ExtendWith(MockKExtension::class)
internal class RecentBranchesServiceTest {
  @MockK
  private lateinit var gatewayMock: RecentBranchesLocalGateway

  private lateinit var service: RecentBranchesService

  @BeforeEach
  fun beforeEach() {
    service = RecentBranchesService(gatewayMock)
  }

  @Test
  fun `stores branches if switch from master to develop`(@MockK repositoryMock: GtRepository) {
    // given
    val master = GitLocalBranch("master")
    val develop = GitLocalBranch("develop")
    val now = Instant.now()

    val recentBranch1 = RecentBranch("branch1", now.minusSeconds(1).epochSecond)
    val branch1 = GitLocalBranch(recentBranch1.branchName)
    val recentBranch2 = RecentBranch("branch2", now.minusSeconds(2).epochSecond)
    val branch2 = GitLocalBranch(recentBranch2.branchName)
    val recentBranch3 = RecentBranch("branch3", now.minusSeconds(3).epochSecond)
    val branch3 = GitLocalBranch(recentBranch3.branchName)
    val recentBranch4 = RecentBranch("branch4", now.minusSeconds(4).epochSecond)
    val branch4 = GitLocalBranch(recentBranch4.branchName)
    val recentBranch5 = RecentBranch("branch5", now.minusSeconds(5).epochSecond)
    val branch5 = GitLocalBranch(recentBranch5.branchName)

    every { gatewayMock.now() } returns now
    every {
      gatewayMock.getRecentBranchesFromStore(repositoryMock)
    } returns
      listOf(recentBranch5, recentBranch4, recentBranch3, recentBranch2, recentBranch1)
    every { repositoryMock.findLocalBranch(branch1.name) } returns branch1
    every { repositoryMock.findLocalBranch(branch2.name) } returns branch2
    every { repositoryMock.findLocalBranch(branch3.name) } returns branch3
    every { repositoryMock.findLocalBranch(branch4.name) } returns branch4
    every { repositoryMock.findLocalBranch(branch5.name) } returns branch5

    every { gatewayMock.storeRecentBranches(any(), repositoryMock) } just Runs

    // when
    service.branchSwitch(master, develop, repositoryMock)

    // then
    verify {
      gatewayMock.storeRecentBranches(
        listOf(
          RecentBranch(develop.name, now.epochSecond),
          recentBranch1,
          recentBranch2,
          recentBranch3,
          recentBranch4
        ),
        repositoryMock
      )
    }
  }

  @Test
  fun `recent branches do not return non existing local branches`(@MockK repositoryMock: GtRepository) {
    // given
    val now = Instant.now()
    val recentBranch1 = RecentBranch("branch1", now.minusSeconds(1).epochSecond)
    val branch1 = GitLocalBranch(recentBranch1.branchName)
    val recentBranch2 = RecentBranch("branch2", now.minusSeconds(2).epochSecond)
    val recentBranch3 = RecentBranch("branch3", now.minusSeconds(3).epochSecond)
    val recentBranch4 = RecentBranch("branch4", now.minusSeconds(4).epochSecond)
    val recentBranch5 = RecentBranch("branch5", now.minusSeconds(5).epochSecond)
    val recentDevelop = RecentBranch("develop", now.minusSeconds(6).epochSecond)
    val develop = GitLocalBranch(recentDevelop.branchName)

    every {
      gatewayMock.getRecentBranchesFromStore(repositoryMock)
    } returns
      listOf(recentBranch1, recentBranch2, recentBranch3, recentBranch4, recentBranch5, recentDevelop)
    every { repositoryMock.findLocalBranch(recentBranch1.branchName) } returns branch1
    every { repositoryMock.findLocalBranch(recentBranch2.branchName) } returns null
    every { repositoryMock.findLocalBranch(recentBranch3.branchName) } returns null
    every { repositoryMock.findLocalBranch(recentBranch4.branchName) } returns null
    every { repositoryMock.findLocalBranch(recentBranch5.branchName) } returns null
    every { repositoryMock.findLocalBranch(recentDevelop.branchName) } returns develop

    // when
    val branches = service.getRecentBranches(repositoryMock)

    // then
    assertThat(branches).containsExactly(develop, branch1)
  }
}
