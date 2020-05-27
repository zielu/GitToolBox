package zielu.gittoolbox.status

import org.eclipse.jgit.api.Git
import org.junit.Ignore
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.extension.ExtendWith
import zielu.IntegrationTest
import zielu.junit5.intellij.extension.git.GitTest
import zielu.junit5.intellij.extension.git.GitTestExtension
import zielu.junit5.intellij.extension.git.GitTestSetup
import zielu.junit5.intellij.extension.platform.HeavyPlatformTestCaseExtension
import zielu.junit5.intellij.extension.resources.ExternalPath
import zielu.junit5.intellij.extension.resources.ResourcesExtension
import java.nio.file.Path

@Ignore
@IntegrationTest
@ExtendWith(HeavyPlatformTestCaseExtension::class, GitTestExtension::class, ResourcesExtension::class)
internal class GitStatusCalculatorIntegrationTest {
    private lateinit var myTestDataPath: Path

    @BeforeAll
    @Throws(Exception::class)
    fun beforeAll(
      gitTest: GitTest,
      @ExternalPath(".", "testDataDynamic", "it", "GitStatusIT") testDataPath: Path
    ) {
        myTestDataPath = testDataPath
        initGit(gitTest, myTestDataPath)
    }

    private fun initGit(gitTest: GitTest, rootPath: Path) {
        val upstream1RootPath = rootPath.resolve("upstream1")
        val upstream2RootPath = rootPath.resolve("upstream2")

        gitTest.prepare(object : GitTestSetup {
            override fun getRootPath(): Path = upstream1RootPath

            override fun setup(git: Git) {
                // do nothing
            }
        })
        gitTest.prepare(object : GitTestSetup {
            override fun getRootPath(): Path = upstream2RootPath

            override fun setup(git: Git) {
                // do nothing
            }
        })
        gitTest.prepare(object : GitTestSetup {
            override fun getRootPath(): Path {
                return rootPath.resolve("repo")
            }

            override fun setup(git: Git) {
                TODO("Not yet implemented")
            }
        })
    }
}
