package zielu.gittoolbox.completion.gitmoji

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.util.TextRange
import zielu.gittoolbox.completion.CompletionProviderBase
import zielu.gittoolbox.completion.CompletionService
import zielu.gittoolbox.config.AppConfig

internal class GitmojiCompletionProvider : CompletionProviderBase() {

  override fun setupCompletions(project: Project, result: CompletionResultSet) {
    if (isEnabled()) {
      val completionService = CompletionService.getInstance(project)
      if (completionService.affected.isNotEmpty()) {
        addCompletions(result)
      }
    }
  }

  private fun isEnabled(): Boolean = AppConfig.getConfig().commitDialogGitmojiCompletion

  private fun addCompletions(result: CompletionResultSet) {
    GitmojiResBundle.keySet().forEach { gitmoji ->
      val description = GitmojiResBundle.message(gitmoji)
      val icon = IconLoader.findIcon("/zielu/gittoolbox/gitmoji/$gitmoji.png", javaClass, true, false)
      var builder = LookupElementBuilder.create(":$gitmoji:")
        .withTypeText(description)
        .withIcon(icon)
        .withInsertHandler(PrefixCompletionInsertHandler(gitmoji))

      val wordsList = GitmojiMetadata.getKeywords(gitmoji)
      if (wordsList.isNotEmpty()) {
        builder = builder.withLookupStrings(wordsList)
      }
      result.addElement(builder)
    }
  }
}

private class PrefixCompletionInsertHandler(private val gitmoji: String) : InsertHandler<LookupElement> {
  override fun handleInsert(context: InsertionContext, item: LookupElement) {
    val gitmojiLookup = item.lookupString
    var startOffset = context.startOffset - 1
    if (startOffset < 0) {
      startOffset = 0
    }
    val useUnicode = AppConfig.getConfig().commitDialogGitmojiCompletionUnicode
    val textBeforeOffsets = context.document.getText(TextRange(startOffset, context.tailOffset))
    if (textBeforeOffsets.startsWith(":$gitmojiLookup")) {
      context.document.replaceString(startOffset, context.tailOffset, replacement(gitmojiLookup, useUnicode))
    } else if (useUnicode) {
      context.document.replaceString(context.startOffset, context.tailOffset, replacement(gitmojiLookup, true))
    }
  }

  private fun replacement(gitmojiLookup: String, unicode: Boolean): String {
    return if (unicode) {
      GitmojiMetadata.getUnicode(gitmoji)
    } else {
      gitmojiLookup
    }
  }
}
