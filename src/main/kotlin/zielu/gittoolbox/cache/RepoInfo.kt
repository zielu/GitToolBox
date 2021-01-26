package zielu.gittoolbox.cache

import zielu.gittoolbox.status.GitAheadBehindCount
import java.util.Optional

internal data class RepoInfo(
  val status: RepoStatus,
  val count: GitAheadBehindCount?,
  val tags: List<String>
) {
  fun isEmpty(): Boolean = status.isEmpty && count == null

  fun tagsNotEmpty(): Boolean = tags.isNotEmpty()

  @Deprecated("for Java compatibility", replaceWith = ReplaceWith("count?.") )
  fun maybeCount(): Optional<GitAheadBehindCount> = Optional.ofNullable(count)

  companion object {
    private val empty: RepoInfo = RepoInfo(RepoStatus.empty(), null, listOf())

    @JvmStatic
    fun create(
      status: RepoStatus,
      count: GitAheadBehindCount?,
      tags: Collection<String>
    ): RepoInfo {
      return RepoInfo(status, count, tags.toSet().toList())
    }

    @JvmStatic
    fun empty(): RepoInfo = empty
  }
}
