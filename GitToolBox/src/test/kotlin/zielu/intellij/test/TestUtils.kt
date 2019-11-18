package zielu.intellij.test

import git4idea.repo.GitRemote

internal fun createRemote(name: String) = GitRemote(name, listOf(), listOf(), listOf(), listOf())
