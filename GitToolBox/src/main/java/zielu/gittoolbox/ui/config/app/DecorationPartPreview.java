package zielu.gittoolbox.ui.config.app;

import static zielu.gittoolbox.config.DecorationPartType.BRANCH;
import static zielu.gittoolbox.config.DecorationPartType.CHANGED_COUNT;
import static zielu.gittoolbox.config.DecorationPartType.LOCATION;
import static zielu.gittoolbox.config.DecorationPartType.STATUS;
import static zielu.gittoolbox.config.DecorationPartType.TAGS_ON_HEAD;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import jodd.util.StringBand;
import zielu.gittoolbox.config.DecorationPartType;
import zielu.gittoolbox.ui.StatusPresenter;

class DecorationPartPreview {
  private static final Map<DecorationPartType, String> PREVIEWS = ImmutableMap.of(BRANCH, "master",
      LOCATION, "/path/to/location", TAGS_ON_HEAD, "1.0.0", CHANGED_COUNT, "1 changed");

  private DecorationPartPreview() {
    throw new IllegalStateException();
  }

  static StringBand appendPreview(StatusPresenter presenter, DecorationPartType type, StringBand preview) {
    return preview.append(getPreview(presenter, type));
  }

  private static String getPreview(StatusPresenter presenter, DecorationPartType type) {
    if (type == STATUS) {
      return presenter.aheadBehindStatus(3, 2);
    } else {
      return PREVIEWS.getOrDefault(type, "N/A");
    }
  }
}
