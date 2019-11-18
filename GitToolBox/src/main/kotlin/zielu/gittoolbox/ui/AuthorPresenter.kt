package zielu.gittoolbox.ui

import com.intellij.openapi.vcs.actions.ShortNameType
import zielu.gittoolbox.config.AuthorNameType

internal object AuthorPresenter {
  @JvmStatic
  fun format(type: AuthorNameType, author: String, authorEmail: String?): String? = when (type) {
    AuthorNameType.INITIALS -> ShortNameType.shorten(author, ShortNameType.INITIALS)
    AuthorNameType.FIRSTNAME -> ShortNameType.shorten(author, ShortNameType.FIRSTNAME)
    AuthorNameType.LASTNAME -> ShortNameType.shorten(author, ShortNameType.LASTNAME)
    AuthorNameType.FULL -> ShortNameType.shorten(author, ShortNameType.NONE)
    AuthorNameType.EMAIL -> authorEmail
    AuthorNameType.EMAIL_USER -> authorEmail?.substringBefore('@')
  }
}
