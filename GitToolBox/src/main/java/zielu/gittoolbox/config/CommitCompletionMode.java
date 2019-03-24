package zielu.gittoolbox.config;

import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.ResBundle;

public enum CommitCompletionMode {
  AUTOMATIC("auto"),
  ON_DEMAND("on.demand");

  private final String labelKey;

  CommitCompletionMode(String labelKey) {
    this.labelKey = "commit.dialog.completion.mode." + labelKey + ".label";
  }

  @NotNull
  public String getDisplayLabel() {
    return ResBundle.message(labelKey);
  }
}
