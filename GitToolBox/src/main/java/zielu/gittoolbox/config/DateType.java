package zielu.gittoolbox.config;

import com.intellij.util.xmlb.annotations.Transient;
import zielu.gittoolbox.ResBundle;

public enum DateType {
  AUTO("date.type.auto"),
  RELATIVE("date.type.relative"),
  ABSOLUTE("date.type.absolute")
  ;

  private final String descriptionKey;

  DateType(String descriptionKey) {
    this.descriptionKey = descriptionKey;
  }

  @Transient
  public String getDescription() {
    return ResBundle.message(descriptionKey);
  }
}
