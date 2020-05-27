package zielu.junit5.intellij.extension.git

import org.eclipse.jgit.api.Git
import java.nio.file.Path

internal interface GitTestSetup {
    fun getRootPath(): Path

    @JvmDefault
    fun isBare(): Boolean = false

    @Throws(Exception::class)
    fun setup(git: Git)
}
