package zielu.gittoolbox.revision

import com.intellij.openapi.project.Project
import zielu.gittoolbox.util.AppUtil
import java.time.ZonedDateTime

internal class RevisionInfoFactory {
  fun forLine(provider: RevisionDataProvider, lineNumber: Int): RevisionInfo {
    val authorDateTime = prepareDate(provider.getAuthorDateTime(lineNumber))
    val author = prepareAuthor(provider.getAuthor(lineNumber))
    val authorEmail = provider.getAuthorEmail(lineNumber)
    val subject = provider.getSubject(lineNumber)
    val revisionNumber = provider.getRevisionNumber(lineNumber)

    return RevisionInfoImpl(
      revisionNumber,
      author,
      authorDateTime,
      subject,
      authorEmail
    )
  }

  private fun prepareAuthor(author: String?): String {
    return author?.trim { it <= ' ' }?.replace("\\(.*\\)".toRegex(), "") ?: "EMPTY"
  }

  private fun prepareDate(revisionDate: ZonedDateTime?): ZonedDateTime {
    return revisionDate ?: ZonedDateTime.now()
  }

  companion object {
    fun getInstance(project: Project): RevisionInfoFactory {
      return AppUtil.getServiceInstance(project, RevisionInfoFactory::class.java)
    }
  }
}
