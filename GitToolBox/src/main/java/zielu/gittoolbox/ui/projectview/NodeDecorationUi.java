package zielu.gittoolbox.ui.projectview;

import static zielu.gittoolbox.config.DecorationPartType.LOCATION;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jodd.util.StringBand;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zielu.gittoolbox.config.DecorationPartConfig;
import zielu.gittoolbox.config.DecorationPartType;
import zielu.gittoolbox.config.GitToolBoxConfig2;
import zielu.gittoolbox.ui.StatusPresenter;

public class NodeDecorationUi {
  protected final GitToolBoxConfig2 config;
  private final Map<DecorationPartType, DecorationPartConfig> configuredParts = new LinkedHashMap<>();

  public NodeDecorationUi(@NotNull GitToolBoxConfig2 config) {
    this.config = config;
    config.getDecorationParts().forEach(part -> configuredParts.put(part.type, part));
  }

  public StatusPresenter getPresenter() {
    return config.getPresenter();
  }

  public Collection<DecorationPartType> getDecorationTypes() {
    return Collections.unmodifiableSet(configuredParts.keySet());
  }

  public boolean hasLocationPart() {
    return configuredParts.containsKey(LOCATION);
  }

  public boolean isLocationPartLast() {
    List<DecorationPartConfig> parts = config.getDecorationParts();
    return !parts.isEmpty() && parts.get(parts.size() - 1).type == LOCATION;
  }

  @Nullable
  public String getDecorationPartText(@Nullable String value, DecorationPartType type) {
    if (StringUtils.isNotBlank(value)) {
      DecorationPartConfig part = configuredParts.get(type);
      return new StringBand(part.prefix).append(value).append(part.postfix).toString();
    }
    return null;
  }
}
