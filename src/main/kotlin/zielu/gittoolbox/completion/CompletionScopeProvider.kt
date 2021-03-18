package zielu.gittoolbox.completion

import java.io.File

internal interface CompletionScopeProvider {
  fun getAffectedFiles(): Collection<File>

  companion object {
    val empty: CompletionScopeProvider = object : CompletionScopeProvider {
      override fun getAffectedFiles(): Collection<File> = listOf()
    }
  }
}
