package zielu.gittoolbox.ui.branch

import com.intellij.openapi.ui.popup.ListPopupStepEx
import com.intellij.openapi.ui.popup.ListSeparator
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.util.ui.StatusText

internal class RecentBranchesListPopupStep(
  title: String,
  items: List<RecentBranchItem>
) : BaseListPopupStep<RecentBranchItem>(title, items), ListPopupStepEx<RecentBranchItem> {

  override fun onChosen(selectedValue: RecentBranchItem, finalChoice: Boolean, eventModifiers: Int): PopupStep<*> {
    selectedValue.onChosen()
    return PopupStep.FINAL_CHOICE
  }

  override fun setEmptyText(emptyText: StatusText) {
  }

  override fun getTooltipTextFor(value: RecentBranchItem): String? = null

  override fun getTextFor(value: RecentBranchItem): String {
    return value.getText()
  }

  override fun getSeparatorAbove(value: RecentBranchItem): ListSeparator? {
    return value.getSeparatorText()?.let { ListSeparator(it) }
  }
}
