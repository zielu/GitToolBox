package zielu.gittoolbox.checkin

import com.intellij.openapi.vcs.checkin.VcsCheckinHandlerFactory
import git4idea.GitVcs

internal abstract class GitBaseCheckinHandlerFactory : VcsCheckinHandlerFactory(GitVcs.getKey())
