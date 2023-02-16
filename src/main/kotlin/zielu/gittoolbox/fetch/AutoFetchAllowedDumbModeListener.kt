package zielu.gittoolbox.fetch

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project

internal class AutoFetchAllowedDumbModeListener(private val project: Project) : DumbService.DumbModeListener {
  override fun enteredDumbMode() {
    AutoFetchAllowedDumbMode.getInstance(project).enteredDumbMode()
  }

  override fun exitDumbMode() {
    AutoFetchAllowedDumbMode.getInstance(project).leftDumbMode()
  }
}
