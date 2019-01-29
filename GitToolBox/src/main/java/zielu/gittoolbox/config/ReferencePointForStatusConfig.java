package zielu.gittoolbox.config;

import org.apache.commons.lang3.builder.EqualsBuilder;

public class ReferencePointForStatusConfig {
  public ReferencePointForStatus referencePointForStatus = ReferencePointForStatus.TRACKED_REMOTE_BRANCH;
  public String referencePointForStatusName;

  public boolean isChanged(ReferencePointForStatusConfig config) {
    return new EqualsBuilder()
        .append(referencePointForStatus, config.referencePointForStatus)
        .append(referencePointForStatusName, config.referencePointForStatusName)
        .build();
  }
}
