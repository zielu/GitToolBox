package zielu.gittoolbox.metrics;

import com.intellij.openapi.Disposable;

interface MetricsReporter extends Disposable {
  MetricsReporter EMPTY = () -> {
    //do nothing
  };
}
