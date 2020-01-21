package zielu.gittoolbox.lifecycle

import com.intellij.openapi.project.Project
import com.intellij.util.messages.Topic

internal interface ProjectLifecycleNotifier {
  /**
   * Not called for default project
   */
  fun projectReady(project: Project) {
    // default implementation
  }

  companion object {
    @JvmField
    val TOPIC = Topic.create("Git ToolBox Project Lifecycle", ProjectLifecycleNotifier::class.java)
  }
}
