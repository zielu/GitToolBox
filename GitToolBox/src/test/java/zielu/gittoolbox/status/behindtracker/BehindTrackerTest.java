package zielu.gittoolbox.status.behindtracker;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static zielu.intellij.test.MockVfsUtil.createDir;

import com.google.common.collect.ImmutableList;
import com.intellij.mock.MockVirtualFile;
import com.intellij.openapi.project.Project;
import com.intellij.vcs.log.Hash;
import com.intellij.vcs.log.impl.HashImpl;
import git4idea.GitLocalBranch;
import git4idea.GitRemoteBranch;
import git4idea.GitStandardRemoteBranch;
import git4idea.repo.GitRemote;
import git4idea.repo.GitRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zielu.gittoolbox.cache.RepoInfo;
import zielu.gittoolbox.cache.RepoStatus;
import zielu.gittoolbox.status.GitAheadBehindCount;
import zielu.gittoolbox.ui.StatusMessages;
import zielu.gittoolbox.ui.StatusMessagesUi;
import zielu.gittoolbox.ui.StatusPresenters;
import zielu.gittoolbox.ui.behindtracker.BehindTrackerUi;

@Tag("fast")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
class BehindTrackerTest {
  private static final Hash LOCAL_HASH = HashImpl.build("92c4b38ed6cc6f2091f454d177074fceb70d5a80");
  private static final GitLocalBranch LOCAL_BRANCH = new GitLocalBranch("master");
  private static final Hash REMOTE_HASH_1 = HashImpl.build("2928c843afc39e677f3dc123d1da49b83298f78a");
  private static final GitRemote REMOTE = new GitRemote("origin", emptyList(), emptyList(), emptyList(),
      emptyList());
  private static final Hash REMOTE_HASH_2 = HashImpl.build("2eb9b31b1ec2d9e01587031d87f2c34b57d89ea5");
  private static final GitRemoteBranch REMOTE_BRANCH = new GitStandardRemoteBranch(REMOTE, "master");

  private static final RepoStatus REPO_STATUS_1 = RepoStatus.create(LOCAL_BRANCH, LOCAL_HASH, REMOTE_BRANCH,
      REMOTE_HASH_1);
  private static final RepoStatus REPO_STATUS_2 = RepoStatus.create(LOCAL_BRANCH, LOCAL_HASH, REMOTE_BRANCH,
      REMOTE_HASH_2);
  private static final RepoInfo REPO_INFO_1 = RepoInfo.create(REPO_STATUS_1,
      GitAheadBehindCount.success(1, LOCAL_HASH, 1, REMOTE_HASH_1), ImmutableList.of());
  private static final RepoInfo REPO_INFO_2 = RepoInfo.create(REPO_STATUS_2,
      GitAheadBehindCount.success(1, LOCAL_HASH, 2, REMOTE_HASH_2), ImmutableList.of());
  private final Logger log = LoggerFactory.getLogger(getClass());
  @Mock
  private BehindTrackerUi behindTrackerUi;
  @Mock
  private StatusMessagesUi statusMessagesUi;
  @Mock
  private GitRepository repository;
  @Mock
  private Project project;
  private MockVirtualFile repositoryRoot = createDir("repoRoot");
  @Captor
  private ArgumentCaptor<String> notificationCaptor;
  private BehindTracker behindTracker;
  private StatusMessages statusMessages;

  @BeforeEach
  void beforeEach() {
    when(repository.getProject()).thenReturn(project);
    when(repository.getRoot()).thenReturn(repositoryRoot);
    when(statusMessagesUi.presenter()).thenReturn(StatusPresenters.arrows);
    statusMessages = new StatusMessages(statusMessagesUi);
    when(behindTrackerUi.isNotificationEnabled()).thenReturn(true);
    when(behindTrackerUi.getStatusMessages()).thenReturn(statusMessages);
    behindTracker = new BehindTracker(behindTrackerUi);
    behindTracker.projectOpened();
  }

  @AfterEach
  void afterEach() {
    behindTracker.projectClosed();
  }

  @Test
  void displayDeltaNotificationIfStateChanged() {
    behindTracker.onStateChange(repository, REPO_INFO_1);
    behindTracker.showChangeNotification();
    behindTracker.onStateChange(repository, REPO_INFO_2);
    behindTracker.showChangeNotification();

    verify(behindTrackerUi).displaySuccessNotification(notificationCaptor.capture());

    String notification = notificationCaptor.getValue();
    log.info(notification);
    assertThat(notification).contains("2↓ ∆1");
  }

  @Test
  void dontDisplayDeltaNotificationIfStateNotChanged() {
    behindTracker.onStateChange(repository, REPO_INFO_1);
    behindTracker.showChangeNotification();
    behindTracker.onStateChange(repository, REPO_INFO_1);
    behindTracker.showChangeNotification();

    verify(behindTrackerUi, never()).displaySuccessNotification(notificationCaptor.capture());
  }
}