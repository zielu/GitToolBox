package zielu.gittoolbox.config;

import com.intellij.openapi.vcs.actions.ShortNameType;
import com.intellij.util.xmlb.annotations.Transient;
import org.jetbrains.annotations.Nullable;

import java.util.function.UnaryOperator;

public enum AuthorNameType {
  INITIALS(ShortNameType.INITIALS),
  LASTNAME(ShortNameType.LASTNAME),
  FIRSTNAME(ShortNameType.FIRSTNAME),
  FULL(ShortNameType.NONE),
  EMAIL(email -> email, "Email"),
  USERNAME(email -> {
    if (email == null) {
      return null;
    }
    int atIndex = email.indexOf('@');
    return atIndex == -1 ? email : email.substring(0, atIndex);
  }, "Username from Email");

  private final ShortNameType type;
  private final UnaryOperator<String> shortener;
  private final String description;

  AuthorNameType(ShortNameType type) {
    this.type = type;
    this.description = null;
    this.shortener = null;
  }

  AuthorNameType(UnaryOperator<String> shortener, String description) {
    this.type = null;
    this.shortener = shortener;
    this.description = description;
  }

  @Transient
  public String getDescription() {
    if (type == null) {
      return description;
    }
    return type.getDescription();
  }

  @Nullable
  public String shorten(@Nullable String author) {
    if (type == null) {
      return shortener.apply(author);
    }
    return ShortNameType.shorten(author, type);
  }

  public static AuthorNameType[] inlineBlame() {
    return values();
  }

  public static AuthorNameType[] statusBlame() {
    return values();
  }
}
