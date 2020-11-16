package zielu.gittoolbox.blame.calculator.persistence

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vcs.history.VcsRevisionNumber
import com.intellij.openapi.vfs.VirtualFile
import zielu.gittoolbox.revision.RevisionDataProvider
import zielu.gittoolbox.util.AppUtil
import java.time.Clock
import java.time.Duration

@State(name = "GitToolBoxBlamePersistence", storages = [Storage(StoragePathMacros.CACHE_FILE)])
internal class BlameCalculationPersistence(
  private val project: Project
) : PersistentStateComponent<BlameState> {
  private var state: BlameState = BlameState()

  override fun getState(): BlameState = state

  override fun loadState(state: BlameState) {
    this.state = state
  }

  override fun initializeComponent() {
    cleanGarbage()
  }

  fun storeBlame(revisionData: RevisionDataProvider) {
    if (revisionData.baseRevision != VcsRevisionNumber.NULL) {
      val fileBlameState = BlameCodec.toPersistent(revisionData)
      fileBlameState.accessTimestamp = nowTimestamp()
      val path = filePath(revisionData.file)
      val key = path + ";" + revisionData.baseRevision.asString()
      state.fileBlames[key] = fileBlameState
      log.info("Stored blame: $key")
      cleanGarbage()
    }
  }

  private fun filePath(file: VirtualFile): String {
    return FileUtil.getRelativePath(project.basePath!!, file.path, '/') ?: file.path
  }

  fun getBlame(file: VirtualFile, revision: VcsRevisionNumber): RevisionDataProvider? {
    val path = filePath(file)
    val key = path + ";" + revision.asString()
    val fileBlameState = state.fileBlames[key]
    return fileBlameState?.let {
      it.accessTimestamp = nowTimestamp()
      BlameCodec.fromPersistent(file, it)
    }?.let {
      log.info("Restored blame: $key")
      it
    }
  }

  private fun nowTimestamp(): Long = Clock.systemUTC().millis()

  private fun cleanGarbage() {
    val ttlBound = nowTimestamp() - ttlMillis
    state.fileBlames.entries.removeIf { it.value.accessTimestamp < ttlBound }

    val overflow = state.fileBlames.size - maxSize
    if (overflow > 0) {
      log.info("Remove overflowing entries:  $overflow")
      val toRemove = state.fileBlames.entries
        .sortedBy { it.value.accessTimestamp }
        .take(overflow)
        .map { it.key }
      toRemove.forEach { state.fileBlames.remove(it) }
    }
  }

  companion object {
    private val log = Logger.getInstance(BlameCalculationPersistence::class.java)
    private val ttlMillis = Duration.ofDays(7).toMillis()
    private const val maxSize = 30

    @JvmStatic
    fun getInstance(project: Project): BlameCalculationPersistence {
      return AppUtil.getServiceInstance(project, BlameCalculationPersistence::class.java)
    }
  }
}
