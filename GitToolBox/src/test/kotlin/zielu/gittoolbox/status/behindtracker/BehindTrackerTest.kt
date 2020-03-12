package zielu.gittoolbox.status.behindtracker

import com.google.common.collect.ImmutableList
import com.intellij.vcs.log.impl.HashImpl
import git4idea.GitLocalBranch
import git4idea.GitRemoteBranch
import git4idea.GitStandardRemoteBranch
import git4idea.repo.GitRemote
import git4idea.repo.GitRepository
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import zielu.gittoolbox.cache.RepoInfo
import zielu.gittoolbox.cache.RepoStatus
import zielu.gittoolbox.cache.RepoStatusRemote
import zielu.gittoolbox.repo.GtRepository
import zielu.gittoolbox.status.GitAheadBehindCount

@ExtendWith(MockKExtension::class)
internal class BehindTrackerTest {
  private val LOCAL_HASH = HashImpl.build("92c4b38ed6cc6f2091f454d177074fceb70d5a80")
  private val LOCAL_BRANCH = GitLocalBranch("master")
  private val REMOTE_HASH_1 = HashImpl.build("2928c843afc39e677f3dc123d1da49b83298f78a")
  private val REMOTE = GitRemote("origin", emptyList(), emptyList(), emptyList(),
    emptyList())
  private val REMOTE_HASH_2 = HashImpl.build("2eb9b31b1ec2d9e01587031d87f2c34b57d89ea5")
  private val REMOTE_BRANCH: GitRemoteBranch = GitStandardRemoteBranch(REMOTE, "master")

  private val REPO_STATUS_1 = RepoStatus.create(LOCAL_BRANCH, LOCAL_HASH,
    RepoStatusRemote(REMOTE_BRANCH, REMOTE_HASH_1))
  private val REPO_STATUS_2 = RepoStatus.create(LOCAL_BRANCH, LOCAL_HASH,
    RepoStatusRemote(REMOTE_BRANCH, REMOTE_HASH_2))
  private val REPO_INFO_1 = RepoInfo.create(REPO_STATUS_1,
    GitAheadBehindCount.success(1, LOCAL_HASH, 1, REMOTE_HASH_1), ImmutableList.of())
  private val REPO_INFO_2 = RepoInfo.create(REPO_STATUS_2,
    GitAheadBehindCount.success(1, LOCAL_HASH, 2, REMOTE_HASH_2), ImmutableList.of())
  private val BEHIND_STATUS_VALUE = "behind state"

  @MockK
  private lateinit var gatewayMock: BehindTrackerLocalGateway
  @MockK
  private lateinit var repositoryMock: GitRepository
  @MockK
  private lateinit var gtRepositoryMock: GtRepository
  private lateinit var behindTracker: BehindTracker

  @BeforeEach
  fun beforeEach() {
    every { gatewayMock.isNotificationEnabled() } returns true
    every { gatewayMock.getGtRepository(repositoryMock) } returns gtRepositoryMock
    every { gatewayMock.disposeWithProject(any()) } returns Unit
    every { gtRepositoryMock.getName() } returns ""
    behindTracker = BehindTracker(gatewayMock)
  }

  @AfterEach
  fun afterEach() {
    behindTracker.dispose()
  }

  @Test
  fun `should display delta notification if state changed`() {
    // given
    every { gatewayMock.prepareBehindMessage(any(), any()) } returns BEHIND_STATUS_VALUE
    val notificationSlot = slot<String>()
    every { gatewayMock.displaySuccessNotification(capture(notificationSlot)) } returns Unit

    // when
    behindTracker.onStateChange(repositoryMock, REPO_INFO_1)
    behindTracker.showChangeNotification()
    behindTracker.onStateChange(repositoryMock, REPO_INFO_2)
    behindTracker.showChangeNotification()

    // then
    assertThat(notificationSlot.captured).contains(BEHIND_STATUS_VALUE)
  }

  @Test
  fun `should not display delta notification if state is not changed`() {
    behindTracker.onStateChange(repositoryMock, REPO_INFO_1)
    behindTracker.showChangeNotification()
    behindTracker.onStateChange(repositoryMock, REPO_INFO_1)
    behindTracker.showChangeNotification()

    verify(exactly = 0) { gatewayMock.displaySuccessNotification(any()) }
  }
}
