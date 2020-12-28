package zielu.gittoolbox.config

import com.intellij.openapi.vcs.actions.ShortNameType
import com.intellij.util.xmlb.annotations.Transient
import zielu.gittoolbox.ResBundle
import java.util.EnumSet

internal enum class AuthorNameType(private val labelSupplier: () -> String) {
  INITIALS(ShortNameType.INITIALS::getDescription),
  LASTNAME(ShortNameType.LASTNAME::getDescription),
  FIRSTNAME(ShortNameType.FIRSTNAME::getDescription),
  FULL(ShortNameType.NONE::getDescription),
  EMAIL({ ResBundle.message("author.name.type.email") }),
  EMAIL_USER({ ResBundle.message("author.name.type.email.user") }),
  HIDDEN({ ResBundle.message("author.name.type.hidden") })
  ;

  @Transient
  fun getDisplayLabel() = labelSupplier.invoke()

  companion object {
    val allValues = EnumSet.allOf(AuthorNameType::class.java).toList()
    @JvmStatic
    val inlineBlame = values().toList()
    @JvmStatic
    val statusBlame = values().toList()
  }
}
