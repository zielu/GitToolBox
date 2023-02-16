package zielu.gittoolbox.completion.gitmoji

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiPlainText

internal class GitmojiCompletionContributor : CompletionContributor() {
  init {
    extend(CompletionType.BASIC, PlatformPatterns.psiElement(PsiPlainText::class.java), GitmojiCompletionProvider())
  }
}
