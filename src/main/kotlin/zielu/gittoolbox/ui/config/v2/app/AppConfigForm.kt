package zielu.gittoolbox.ui.config.v2.app

import com.intellij.openapi.util.Disposer
import com.intellij.ui.components.JBTabbedPane
import zielu.gittoolbox.ResBundle
import zielu.gittoolbox.config.MutableConfig
import zielu.intellij.ui.CompositeGtFormUiEx
import zielu.intellij.ui.GtFormUiEx
import javax.swing.JComponent

internal class AppConfigForm : GtFormUiEx<MutableConfig> {
  private val pages = CompositeGtFormUiEx<MutableConfig>()
  private val tabs = JBTabbedPane()

  init {
    Disposer.register(this, pages)
  }

  override fun getContent(): JComponent {
    return tabs
  }

  override fun afterStateSet() {
    pages.afterStateSet()
  }

  override fun init() {
    val appPages = AppPages()
    val generalPage = GeneralPage(appPages)
    val projectViewPage = ProjectViewPage(appPages)
    val blamePage = BlamePage()
    val autoFetchPages = AutoFetchPage()
    val statusPage = StatusPage()
    val commitPage = CommitPage()
    pages.add(generalPage)
    pages.add(projectViewPage)
    pages.add(autoFetchPages)
    pages.add(statusPage)
    pages.add(blamePage)
    pages.add(commitPage)
    pages.init()
    tabs.addTab(ResBundle.message("configurable.app.general.tab.title"), generalPage.content)
    tabs.addTab(ResBundle.message("configurable.app.projectView.tab.title"), projectViewPage.content)
    tabs.addTab(ResBundle.message("configurable.app.blame.tab.title"), blamePage.content)
    tabs.addTab(ResBundle.message("configurable.app.autoFetch.tab.title"), autoFetchPages.content)
    tabs.addTab(ResBundle.message("configurable.app.status.tab.title"), statusPage.content)
    tabs.addTab(ResBundle.message("configurable.app.commit.tab.title"), commitPage.content)
  }

  override fun dispose() {
    pages.dispose()
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
