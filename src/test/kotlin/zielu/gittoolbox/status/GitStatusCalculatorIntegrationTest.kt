package zielu.gittoolbox.status

import com.google.common.base.Charsets
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.URIish
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import zielu.IntegrationTest
import zielu.junit5.intellij.extension.git.GitOps
import zielu.junit5.intellij.extension.git.GitProject
import zielu.junit5.intellij.extension.git.GitTest
import zielu.junit5.intellij.extension.git.GitTestExtension
import zielu.junit5.intellij.extension.git.GitTestSetup
import zielu.junit5.intellij.extension.platform.HeavyPlatformTestCaseExtension
import zielu.junit5.intellij.extension.resources.ExternalPath
import zielu.junit5.intellij.extension.resources.ResourcesExtension
import java.nio.file.Files
import java.nio.file.Path

@Disabled
@IntegrationTest
@ExtendWith(HeavyPlatformTestCaseExtension::class, GitTestExtension::class, ResourcesExtension::class)
internal class GitStatusCalculatorIntegrationTest {
  @BeforeEach
  fun beforeEach(project: Project, module: Module) {
    GitProject.setup(project, module, repoPath)
  }

  @Test
  fun test(gitTest: GitTest) {
    gitTest.ops(
      object : GitOps {
        override fun getRootPath(): Path = repoPath

        override fun invoke(git: Git) {
          git.push().call()
        }
      }
    )
  }

  companion object {
    private const val fileName = "file.txt"
    private lateinit var myTestDataPath: Path
    private lateinit var repoPath: Path

    @BeforeAll
    @Throws(Exception::class)
    @JvmStatic
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

      gitTest.prepare(
        object : GitTestSetup {
          override fun getRootPath(): Path = upstream1RootPath

          override fun setup(git: Git) {
            // do nothing
          }
        }
      )
      gitTest.prepare(
        object : GitTestSetup {
          override fun getRootPath(): Path = upstream2RootPath

          override fun setup(git: Git) {
            // do nothing
          }
        }
      )
      val inputRepoRoot = rootPath.resolve("inputRepo")
      gitTest.prepare(
        object : GitTestSetup {
          override fun getRootPath(): Path = inputRepoRoot

          override fun setup(git: Git) {
            val addRemote = git.remoteAdd()
            addRemote.setName("origin")
            addRemote.setUri(URIish(upstream1RootPath.toUri().toURL()))
            addRemote.call()

            Files.write(inputRepoRoot.resolve(fileName), setOf("abc"), Charsets.UTF_8)
            git.add().addFilepattern(fileName).call()
            git.commit().setMessage("Initial commit").call()

            git.push().setRemote("origin").call()
          }
        }
      )
      repoPath = rootPath.resolve("repo")
      gitTest.prepare(
        object : GitTestSetup {
          override fun getRootPath(): Path = repoPath

          override fun setup(git: Git) {
            val addRemote = git.remoteAdd()
            addRemote.setName("origin")
            addRemote.setUri(URIish(upstream1RootPath.toUri().toURL()))
            addRemote.call()

            val remoteSetUrl = git.remoteSetUrl()
            remoteSetUrl.setName("origin")
            remoteSetUrl.setPush(true)
            remoteSetUrl.setUri(URIish(upstream2RootPath.toUri().toURL()))
            remoteSetUrl.call()

            git.fetch().setRemote("origin").call()
            git.checkout().setName("master").setCreateBranch(true).setStartPoint("origin/master").call()
          }
        }
      )
    }
  }
}
