package zielu.gittoolbox.fetch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.intellij.openapi.progress.ProgressIndicator;
import git4idea.repo.GitRepository;
import git4idea.update.GitFetchResult;
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
import org.mockito.junit.jupiter.MockitoExtension;
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
        .withExecutor(Executors.newCachedThreadPool())
        .withUi(ui)
        .build(indicator);
  }

  @Test
  void fetchRootsShouldReturnRepositoryIfSuccessful() {
    when(client.fetch(repository)).thenReturn(GitFetchResult.success());

    Collection<GitRepository> repositories = fetcher.fetchRoots(Collections.singletonList(repository));

    assertThat(repositories).containsOnly(repository);
  }

  @Test
  void fetchRootsShouldNotReturnRepositoryWithError() {
    when(client.fetch(repository)).thenReturn(GitFetchResult.error(new Exception("Test error")));

    Collection<GitRepository> repositories = fetcher.fetchRoots(Collections.singletonList(repository));

    assertThat(repositories).isEmpty();
  }

  @Test
  void fetchRootsShouldHandleFetchException() {
    when(client.fetch(repository)).thenThrow(new RuntimeException("Test error"));

    Collection<GitRepository> repositories = fetcher.fetchRoots(Collections.singletonList(repository));

    assertThat(repositories).isEmpty();
    verify(indicator, atLeastOnce()).setFraction(indicatorCaptor.capture());
    assertThat(indicatorCaptor.getAllValues()).endsWith(1.0);
  }
}