package zielu.junit5.intellij.extension.git

import org.eclipse.jgit.api.Git
import java.nio.file.Path

internal interface GitOps {
  fun getRootPath(): Path

  fun invoke(git: Git)
}
