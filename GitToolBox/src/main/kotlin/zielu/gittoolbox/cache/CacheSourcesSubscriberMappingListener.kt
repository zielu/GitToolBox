package zielu.gittoolbox.cache

import com.intellij.dvcs.repo.VcsRepositoryMappingListener
import com.intellij.openapi.project.Project

internal class CacheSourcesSubscriberMappingListener(private val project: Project) : VcsRepositoryMappingListener {
  override fun mappingChanged() {
    CacheSourcesSubscriber.getInstance(project).onDirMappingChanged()
  }
}
