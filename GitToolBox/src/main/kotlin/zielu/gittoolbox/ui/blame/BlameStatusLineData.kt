package zielu.gittoolbox.ui.blame

import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolder
import zielu.gittoolbox.revision.RevisionInfo

internal data class BlameStatusLineData(
  val revisionInfo: RevisionInfo,
  val lineInfo: String?
) {

  fun isSameRevision(revisionInfo: RevisionInfo): Boolean {
    return this.revisionInfo == revisionInfo
  }

  companion object {
    private val KEY = Key<BlameStatusLineData>("GitToolBox-blame-status-line-info")

    @JvmStatic
    fun get(userDataHolder: UserDataHolder): BlameStatusLineData? {
      return userDataHolder.getUserData(KEY)
    }

    @JvmStatic
    fun set(userDataHolder: UserDataHolder, editorData: BlameStatusLineData) {
      userDataHolder.putUserData(KEY, editorData)
    }

    @JvmStatic
    fun clear(userDataHolder: UserDataHolder) {
      userDataHolder.putUserData(KEY, null)
    }
  }
}
