package zielu.gittoolbox.branch

import com.hrakaroo.glob.GlobPattern
import com.hrakaroo.glob.MatchingEngine

internal class OutdatedBranchesExclusions(
  exclusionGlobs: List<String>
) {
  private val matchers: List<MatchingEngine> = exclusionGlobs.map { GlobPattern.compile(it) }

  fun isExcluded(branchName: String): Boolean {
    return matchers.any { it.matches(branchName) }
  }
}
