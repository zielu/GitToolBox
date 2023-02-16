package zielu.gittoolbox.store

import com.intellij.util.xmlb.annotations.Transient

internal data class BranchDeletion(
  var name: String = "",
  var hash: String = ""
) {

  @Transient
  fun copy(): BranchDeletion {
    return BranchDeletion(
      name,
      hash
    )
  }
}
