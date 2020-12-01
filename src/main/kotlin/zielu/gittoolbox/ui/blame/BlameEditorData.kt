package zielu.gittoolbox.ui.blame

import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolder
import zielu.gittoolbox.revision.RevisionInfo

internal data class BlameEditorData(
  val editorLineIndex: Int,
  val lineModified: Boolean,
  val generation: Int,
  val revisionInfo: RevisionInfo
) {

  fun isSameEditorLineIndex(editorLineIndex: Int): Boolean {
    return this.editorLineIndex == editorLineIndex
  }

  fun isSameGeneration(generation: Int): Boolean {
    return this.generation == generation
  }

  companion object {
    private val KEY = Key<BlameEditorData>("GitToolBox-blame-editor")

    @JvmStatic
    fun get(userDataHolder: UserDataHolder): BlameEditorData? {
      return userDataHolder.getUserData(KEY)
    }

    @JvmStatic
    fun set(userDataHolder: UserDataHolder, editorData: BlameEditorData) {
      userDataHolder.putUserData(KEY, editorData)
    }

    @JvmStatic
    fun clear(userDataHolder: UserDataHolder) {
      userDataHolder.putUserData(KEY, null)
    }
  }
}
