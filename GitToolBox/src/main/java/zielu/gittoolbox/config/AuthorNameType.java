package zielu.gittoolbox.config;

import com.intellij.openapi.vcs.actions.ShortNameType;
import com.intellij.util.xmlb.annotations.Transient;
import java.util.function.Supplier;
import zielu.gittoolbox.ResBundle;

public enum AuthorNameType {
  INITIALS(ShortNameType.INITIALS),
  LASTNAME(ShortNameType.LASTNAME),
  FIRSTNAME(ShortNameType.FIRSTNAME),
  FULL(ShortNameType.NONE),
  EMAIL(() -> ResBundle.message("author.name.type.email")),
  EMAIL_USER(() -> ResBundle.message("author.name.type.email.user"))
  ;

  private final Supplier<String> description;

  AuthorNameType(ShortNameType type) {
    description = type::getDescription;
  }

  AuthorNameType(Supplier<String> description) {
    this.description = description;
  }

  @Transient
  public String getDescription() {
    return description.get();
  }

  public static AuthorNameType[] inlineBlame() {
    return values();
  }

  public static AuthorNameType[] statusBlame() {
    return values();
  }
}
