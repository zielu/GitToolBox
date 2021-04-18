package zielu.gittoolbox.ui.config.v2.app

import com.intellij.openapi.util.Disposer
import com.intellij.ui.components.JBTabbedPane
import zielu.gittoolbox.ResBundle
import zielu.gittoolbox.config.MutableConfig
import zielu.gittoolbox.ui.config.v2.shared.AutoFetchPage
import zielu.gittoolbox.ui.config.v2.shared.CommitPage
import zielu.gittoolbox.ui.config.v2.shared.StatusPage
import zielu.intellij.ui.CompositeGtFormUiEx
import zielu.intellij.ui.GtFormUiEx
import javax.swing.JComponent

internal class AppConfigForm : GtFormUiEx<MutableConfig> {
  private val pages = CompositeGtFormUiEx<MutableConfig>()
  private val tabs = JBTabbedPane()

  init {
    Disposer.register(this, pages)
  }

  override val content: JComponent
    get() = tabs

  override fun afterStateSet() {
    pages.afterStateSet()
  }

  override fun init() {
    val appPages = AppPages()
    val generalPage = GeneralPage(appPages)
    val projectViewPage = ProjectViewPage(appPages)
    val blamePage = BlamePage()
    val autoFetchPage = AutoFetchPage()
    val statusPage = StatusPage()
    val commitPage = CommitPage()
    pages.add(generalPage)
    pages.add(projectViewPage)
    pages.add(autoFetchPage)
    pages.add(statusPage)
    pages.add(blamePage)
    pages.add(commitPage)
    pages.init()
    tabs.addTab(ResBundle.message("configurable.app.general.tab.title"), generalPage.content)
    tabs.addTab(ResBundle.message("configurable.app.projectView.tab.title"), projectViewPage.content)
    tabs.addTab(ResBundle.message("configurable.app.blame.tab.title"), blamePage.content)
    tabs.addTab(ResBundle.message("configurable.shared.autoFetch.tab.title"), autoFetchPage.content)
    tabs.addTab(ResBundle.message("configurable.shared.status.tab.title"), statusPage.content)
    tabs.addTab(ResBundle.message("configurable.shared.commit.tab.title"), commitPage.content)
  }

  override fun fillFromState(state: MutableConfig) {
    pages.fillFromState(state)
  }

  override fun isModified(): Boolean {
    return pages.isModified()
  }

  override fun applyToState(state: MutableConfig) {
    pages.applyToState(state)
  }
}
