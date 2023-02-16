package zielu.gittoolbox.ui.blame

import com.intellij.openapi.editor.LineExtensionInfo
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolder
import zielu.gittoolbox.revision.RevisionInfo

internal data class BlameEditorLineData(
  val revisionInfo: RevisionInfo,
  val lineInfo: List<LineExtensionInfo>?
) {

  fun isSameRevision(revisionInfo: RevisionInfo): Boolean {
    return this.revisionInfo == revisionInfo
  }

  companion object {
    private val KEY = Key<BlameEditorLineData>("GitToolBox-blame-editor-line-info")

    @JvmStatic
    fun get(userDataHolder: UserDataHolder): BlameEditorLineData? {
      return userDataHolder.getUserData(KEY)
    }

    @JvmStatic
    fun set(userDataHolder: UserDataHolder, editorData: BlameEditorLineData) {
      userDataHolder.putUserData(KEY, editorData)
    }

    @JvmStatic
    fun clear(userDataHolder: UserDataHolder) {
      userDataHolder.putUserData(KEY, null)
    }
  }
}
