package zielu.gittoolbox.ui.branch

import com.intellij.openapi.ui.popup.ListSeparator
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep

internal class RecentBranchesListPopupStep(
  title: String,
  items: List<RecentBranchItem>
) : BaseListPopupStep<RecentBranchItem>(title, items) {

  override fun onChosen(selectedValue: RecentBranchItem, finalChoice: Boolean): PopupStep<*>? {
    selectedValue.onChosen()
    return super.onChosen(selectedValue, finalChoice)
  }

  override fun getTextFor(value: RecentBranchItem): String {
    return value.getText()
  }

  override fun getSeparatorAbove(value: RecentBranchItem): ListSeparator? {
    return value.getSeparatorText()?.let { ListSeparator(it) }
  }
}
