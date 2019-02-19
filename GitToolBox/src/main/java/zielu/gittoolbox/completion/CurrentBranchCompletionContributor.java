package zielu.gittoolbox.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiPlainText;

public class CurrentBranchCompletionContributor extends CompletionContributor {
  public CurrentBranchCompletionContributor() {
    extend(CompletionType.BASIC, PlatformPatterns.psiElement(PsiPlainText.class),
        new CurrentBranchCompletionProvider());
  }
}
