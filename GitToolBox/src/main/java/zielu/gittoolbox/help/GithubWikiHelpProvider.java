package zielu.gittoolbox.help;

import com.intellij.openapi.help.WebHelpProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GithubWikiHelpProvider extends WebHelpProvider {
  @Nullable
  @Override
  public String getHelpPageUrl(@NotNull String helpTopicId) {
    return "https://github.com/zielu/GitToolBox/wiki/Manual";
  }
}
