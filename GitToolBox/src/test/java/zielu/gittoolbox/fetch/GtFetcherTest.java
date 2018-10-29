package zielu.gittoolbox.fetch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import com.intellij.openapi.progress.ProgressIndicator;
import git4idea.repo.GitRepository;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import zielu.gittoolbox.metrics.MockMetrics;

@Tag("fast")
@ExtendWith(MockitoExtension.class)
class GtFetcherTest {
  @Mock
  private GitRepository repository;
  @Mock
  private ProgressIndicator indicator;
  @Mock
  private GtFetchClient client;
  @Mock
  private GtFetcherUi ui;
  @Captor
  private ArgumentCaptor<Double> indicatorCaptor;

  private GtFetcher fetcher;

  @BeforeEach
  void before() {
    doAnswer(invocation -> {
      Runnable task = invocation.getArgument(0);
      task.run();
      return null;
    }).when(ui).invokeLaterIfNeeded(any());

    fetcher = GtFetcher.builder()
        .withClient(client)
        .withMetrics(new MockMetrics())
        .withExecutor(Executors.newFixedThreadPool(4))
        .withUi(ui)
        .build(indicator);
  }

  @Test
  void fetchRootsShouldReturnRepositoryIfSuccessful() {
    when(client.fetch(repository)).thenReturn(DefaultGtFetchResult.success());

    Collection<GitRepository> repositories = fetcher.fetchRoots(Collections.singletonList(repository));

    assertThat(repositories).containsOnly(repository);
  }

  @Test
  void fetchRootsShouldNotReturnRepositoryWithError() {
    when(client.fetch(repository)).thenReturn(DefaultGtFetchResult.error(new Exception("Test error")));

    Collection<GitRepository> repositories = fetcher.fetchRoots(Collections.singletonList(repository));

    assertThat(repositories).isEmpty();
  }

  @Test
  void fetchRootsShouldHandleFetchException() {
    when(client.fetch(repository)).thenThrow(new RuntimeException("Test error"));

    Collection<GitRepository> repositories = fetcher.fetchRoots(Collections.singletonList(repository));

    assertThat(repositories).isEmpty();
    checkIndicatorDone();
  }

  private void checkIndicatorDone() {
    verify(indicator, atLeastOnce()).setFraction(indicatorCaptor.capture());
    assertThat(indicatorCaptor.getAllValues()).endsWith(1.0);
  }

  @Test
  void fetchRootsShouldHandleMultipleRepos() {
    GitRepository repo2 = mock(GitRepository.class);
    GitRepository repo3 = mock(GitRepository.class);
    GitRepository repo4 = mock(GitRepository.class);
    when(client.fetch(any())).thenAnswer(new Answer<Object>() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        GitRepository repo = invocation.getArgument(0);
        if (repo.equals(repository)) {
          throw new RuntimeException("Test error 1");
        } else if (repo.equals(repo3)) {
          return DefaultGtFetchResult.error(new Exception("Test error 2"));
        } else if (repo.equals(repo2) || repo.equals(repo4)) {
          return DefaultGtFetchResult.success();
        } else {
          fail("Unsupported repo");
          return null;
        }
      }
    });

    Collection<GitRepository> repositories = fetcher.fetchRoots(Lists.newArrayList(repository, repo2, repo3, repo4));
    assertThat(repositories).containsOnly(repo2, repo4);
    checkIndicatorDone();
  }
}