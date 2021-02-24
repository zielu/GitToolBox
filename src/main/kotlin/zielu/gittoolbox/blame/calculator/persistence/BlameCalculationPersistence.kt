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
import zielu.gittoolbox.GitToolBoxException
import zielu.gittoolbox.revision.RevisionDataProvider
import zielu.gittoolbox.util.AppUtil
import java.time.Clock
import java.time.Duration
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@State(name = "GitToolBoxBlamePersistence", storages = [Storage(StoragePathMacros.CACHE_FILE)])
internal class BlameCalculationPersistence(
  private val project: Project
) : PersistentStateComponent<BlameState> {
  private val lock = ReentrantLock()
  private var state: BlameState = BlameState()

  override fun getState(): BlameState {
    lock.withLock {
      return state
    }
  }

  override fun loadState(state: BlameState) {
    lock.withLock {
      this.state = state
    }
  }

  override fun initializeComponent() {
    cleanGarbage()
  }

  fun storeBlame(revisionData: RevisionDataProvider) {
    if (revisionData.baseRevision != VcsRevisionNumber.NULL) {
      lock.withLock {
        storeBlameImpl(revisionData)
      }
    }
  }

  private fun storeBlameImpl(revisionData: RevisionDataProvider) {
    val fileBlameState = BlameCodec.toPersistent(revisionData)
    val path = filePath(revisionData.file)
    val key = path + ";" + revisionData.baseRevision.asString()
    lock.withLock {
      fileBlameState.accessTimestamp = nowTimestamp()
      val modified = state.fileBlames.toMutableMap()
      modified[key] = fileBlameState
      state.fileBlames = modified
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
    lock.withLock {
      val fileBlameState = state.fileBlames[key]
      return fileBlameState?.let {
        it.accessTimestamp = nowTimestamp()
        BlameCodec.fromPersistent(file, it)
      }?.let {
        log.info("Restored blame: $key")
        it
      }
    }
  }

  private fun nowTimestamp(): Long = Clock.systemUTC().millis()

  private fun cleanGarbage() {
    lock.withLock {
      try {
        cleanGarbageImpl()
      } catch (e: NullPointerException) {
        // remove corrupted data
        val corrupted = state.fileBlames
        state.fileBlames = mapOf()
        throw GitToolBoxException("Garbage cleanup failed, corrupted blames: $corrupted", e)
      }
    }
  }

  private fun cleanGarbageImpl() {
    val cleaned = state.fileBlames.toMutableMap()
    val ttlBound = nowTimestamp() - ttlMillis

    val toRemoveByTtl = cleaned.entries
      .filter { it.value.accessTimestamp < ttlBound }
      .map { it.key }
    log.info("Remove outdated entries:  $toRemoveByTtl")

    val overflow = cleaned.size - maxSize
    var toRemove = mutableSetOf<String>()
    if (overflow > 0) {
      toRemove = cleaned.entries
        .sortedBy { it.value.accessTimestamp }
        .take(overflow)
        .map { it.key }
        .toMutableSet()
      log.info("Remove overflowing entries:  $toRemove")
    }
    toRemove.addAll(toRemoveByTtl)
    toRemove.forEach { cleaned.remove(it) }
    state.fileBlames = cleaned
  }

  companion object {
    private val log = Logger.getInstance(BlameCalculationPersistence::class.java)
    private val ttlMillis = Duration.ofDays(7).toMillis()
    private const val maxSize = 15

    @JvmStatic
    fun getInstance(project: Project): BlameCalculationPersistence {
      return AppUtil.getServiceInstance(project, BlameCalculationPersistence::class.java)
    }
  }
}
