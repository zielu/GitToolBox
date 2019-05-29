package zielu.gittoolbox.config;

import com.intellij.openapi.vcs.actions.ShortNameType;
import com.intellij.util.xmlb.annotations.Transient;
import org.jetbrains.annotations.Nullable;

public enum AuthorNameType {
  INITIALS(ShortNameType.INITIALS),
  LASTNAME(ShortNameType.LASTNAME),
  FIRSTNAME(ShortNameType.FIRSTNAME),
  FULL(ShortNameType.NONE);

  private final ShortNameType type;

  AuthorNameType(ShortNameType type) {
    this.type = type;
  }

  @Transient
  public String getDescription() {
    return type.getDescription();
  }

  @Nullable
  public String shorten(@Nullable String author) {
    return ShortNameType.shorten(author, type);
  }

  public static AuthorNameType[] inlineBlame() {
    return values();
  }

  public static AuthorNameType[] statusBlame() {
    return values();
  }
}
