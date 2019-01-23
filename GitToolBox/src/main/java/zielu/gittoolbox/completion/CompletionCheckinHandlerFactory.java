package zielu.gittoolbox.completion;

import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.VcsCheckinHandlerFactory;
import git4idea.GitVcs;
import org.jetbrains.annotations.NotNull;

public class CompletionCheckinHandlerFactory extends VcsCheckinHandlerFactory {

  public CompletionCheckinHandlerFactory() {
    super(GitVcs.getKey());
  }

  @NotNull
  @Override
  protected CheckinHandler createVcsHandler(CheckinProjectPanel panel) {
    CompletionCheckinHandler handler = new CompletionCheckinHandler(panel);
    CompletionService.getInstance(panel.getProject()).setScopeProvider(handler);
    return handler;
  }
}
