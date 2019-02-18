package zielu.gittoolbox.config;

import com.intellij.codeInsight.completion.CompletionParameters;
import org.jetbrains.annotations.NotNull;
import zielu.gittoolbox.ResBundle;

public enum CommitCompletionMode {
  AUTOMATIC("automatic") {
    @Override
    public boolean shouldComplete(@NotNull CompletionParameters parameters) {
      return true;
    }
  },
  ON_DEMAND("on.demand") {
    @Override
    public boolean shouldComplete(@NotNull CompletionParameters parameters) {
      return !parameters.isAutoPopup();
    }
  };

  private final String labelKey;

  CommitCompletionMode(String labelKey) {
    this.labelKey = "commit.dialog.completion.mode." + labelKey + ".label";
  }

  @NotNull
  public String getDisplayLabel() {
    return ResBundle.getString(labelKey);
  }

  public abstract boolean shouldComplete(@NotNull CompletionParameters parameters);


}
