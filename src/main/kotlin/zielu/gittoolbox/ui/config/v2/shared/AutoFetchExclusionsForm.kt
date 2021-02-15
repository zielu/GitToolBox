package zielu.gittoolbox.ui.config.v2.shared

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.AnActionButton
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.LCFlags
import com.intellij.ui.layout.panel
import com.intellij.ui.treeStructure.Tree
import git4idea.repo.GitRepository
import zielu.gittoolbox.ResBundle
import zielu.gittoolbox.ResBundle.message
import zielu.gittoolbox.ResIcons
import zielu.gittoolbox.cache.VirtualFileRepoCache
import zielu.gittoolbox.config.AutoFetchExclusionConfig
import zielu.gittoolbox.config.MutableConfig
import zielu.gittoolbox.config.RemoteConfig
import zielu.gittoolbox.ui.config.common.AutoFetchExclusionTreeRenderer
import zielu.gittoolbox.ui.config.common.AutoFetchExclusionsTreeModel
import zielu.gittoolbox.ui.config.common.GtRemoteChooser
import zielu.gittoolbox.ui.config.prj.GtRepoChooser
import zielu.gittoolbox.util.GtUtil.getRepositories
import zielu.gittoolbox.util.GtUtil.name
import zielu.gittoolbox.util.GtUtil.sort
import zielu.intellij.ui.GtFormUi
import zielu.intellij.ui.GtFormUiEx
import java.util.Comparator
import javax.swing.JComponent
import javax.swing.event.TreeSelectionEvent
import javax.swing.tree.TreeSelectionModel

internal class AutoFetchExclusionsForm : GtFormUi, GtFormUiEx<MutableConfig> {
  private val autoFetchExclusionsModel = AutoFetchExclusionsTreeModel()
  private val autoFetchExclusions = Tree(autoFetchExclusionsModel)

  private lateinit var decorator: ToolbarDecorator
  private lateinit var addRemoteButton: AnActionButton
  private lateinit var removeRemoteButton: AnActionButton
  private lateinit var panel: DialogPanel

  private lateinit var config: MutableConfig

  override fun init() {
    autoFetchExclusions.isRootVisible = false
    autoFetchExclusions.selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION

    decorator = ToolbarDecorator.createDecorator(autoFetchExclusions)
    decorator.setAddActionName(message("configurable.prj.autoFetch.exclusions.add.label"))
    decorator.setAddAction { onAddExclusion() }
    decorator.setRemoveActionName(message("configurable.prj.autoFetch.exclusions.remove.label"))
    decorator.setRemoveAction { onRemoveExclusion() }
    addRemoteButton = object : AnActionButton("Add remote", ResIcons.Plus) {
      init {
        isEnabled = false
      }

      override fun actionPerformed(e: AnActionEvent) {
        onAddRemote()
      }
    }
    removeRemoteButton = object : AnActionButton("Remove remote", ResIcons.Minus) {
      init {
        isEnabled = false
      }

      override fun actionPerformed(e: AnActionEvent) {
        onRemoveRemote()
      }
    }
    decorator.addExtraActions(addRemoteButton, removeRemoteButton)
    autoFetchExclusions.addTreeSelectionListener { onTreeSelection(it) }

    val decoratorPanel = decorator.createPanel()

    panel = panel(LCFlags.fillX) {
      titledRow(ResBundle.message("configurable.prj.autoFetch.exclusions.label")) {
        row {
          decoratorPanel(CCFlags.growX)
        }
      }
    }
  }

  private fun onTreeSelection(event: TreeSelectionEvent) {
    val treePath = event.path
    log.debug("Selection ", treePath)
    when {
      autoFetchExclusionsModel.hasConfigAt(treePath) -> {
        removeRemoteButton.isEnabled = false
        addRemoteButton.isEnabled = true
      }
      autoFetchExclusionsModel.hasRemoteAt(treePath) -> {
        removeRemoteButton.isEnabled = true
        addRemoteButton.isEnabled = false
      }
      else -> {
        removeRemoteButton.isEnabled = false
        addRemoteButton.isEnabled = false
      }
    }
  }

  private fun onAddExclusion() {
    log.debug("Add exclusions...")
    val chooser = GtRepoChooser(project(), content)
    val excluded = getExcludedRepositories(autoFetchExclusionsModel.getConfigs())
    log.debug("Currently excluded: ", excluded)
    chooser.selectedRepositories = excluded
    chooser.setRepositories(getRepositories(project()))
    if (chooser.showAndGet()) {
      log.debug("Exclusions about to change")
      var selectedRepositories = chooser.getSelectedRepositories()
      selectedRepositories = sort(selectedRepositories)
      val selectedRoots = selectedRepositories
        .map { obj: GitRepository -> obj.root }
        .map { obj: VirtualFile -> obj.url }
        .map { repositoryRootPath: String ->
          AutoFetchExclusionConfig(
            repositoryRootPath
          )
        }
      autoFetchExclusionsModel.addConfigs(selectedRoots)
    } else {
      log.debug("Exclusions change cancelled")
    }
  }

  private fun project(): Project = config.project()

  private fun getExcludedRepositories(exclusions: Collection<AutoFetchExclusionConfig>): List<GitRepository> {
    val roots = exclusions.map {
      it.repositoryRootPath
    }
    return VirtualFileRepoCache.getInstance(project()).findReposForRoots(roots)
  }

  private fun onRemoveExclusion() {
    autoFetchExclusions.selectionPath?.let {
      if (autoFetchExclusionsModel.hasConfigAt(it)) {
        autoFetchExclusionsModel.removeAtPath(it)
      }
    }
  }

  private fun onAddRemote() {
    autoFetchExclusions.selectionPath?.let {
      val config = autoFetchExclusionsModel.getConfigAt(it)!!
      val addedRemotes = addRemotes(config)
      autoFetchExclusionsModel.addRemotes(config, addedRemotes)
      autoFetchExclusionsModel.getPathFor(config)?.let { path ->
        autoFetchExclusions.expandPath(path)
      }
    }
  }

  private fun addRemotes(config: AutoFetchExclusionConfig): List<RemoteConfig> {
    val maybeRepository = VirtualFileRepoCache.getInstance(project()).findRepoForRoot(config.repositoryRootPath)
    if (maybeRepository.isPresent) {
      val chooser = GtRemoteChooser(project(), content)
      val repository = maybeRepository.get()
      chooser.repositoryName = name(repository)
      chooser.remotes = repository.remotes
      chooser.selectedRemotes = config.excludedRemotes.map {
        it.name
      }.toMutableList()
      if (chooser.showAndGet()) {
        val selectedRemotes = chooser.selectedRemotes
        return selectedRemotes.map {
          RemoteConfig(it)
        }
      }
    }
    return listOf()
  }

  private fun onRemoveRemote() {
    autoFetchExclusions.selectionPath?.let {
      if (autoFetchExclusionsModel.hasRemoteAt(it)) {
        autoFetchExclusionsModel.removeAtPath(it)
      }
    }
  }

  fun setVisible(visible: Boolean) {
    panel.isVisible = visible
  }

  override fun getContent(): JComponent = panel

  override fun afterStateSet() {
    if (config.hasProject()) {
      if (project().isDefault) {
        autoFetchExclusions.isEnabled = false
        decorator.disableAddAction()
        decorator.disableRemoveAction()
      }
    }
  }

  override fun fillFromState(state: MutableConfig) {
    config = state
    if (state.hasProject()) {
      autoFetchExclusions.cellRenderer = AutoFetchExclusionTreeRenderer(project())
      autoFetchExclusionsModel.setConfigs(state.prj().autoFetchExclusionConfigs.map { it.copy() })
    }
  }

  override fun isModified(): Boolean {
    if (config.hasProject()) {
      return autoFetchExclusionsModel.getConfigs() != config.prj().autoFetchExclusionConfigs
    }
    return false
  }

  override fun applyToState(state: MutableConfig) {
    if (state.hasProject()) {
      config.prj().autoFetchExclusionConfigs = autoFetchExclusionsModel.getConfigs().map { it.copy() }
    }
  }

  private companion object {
    private val log = Logger.getInstance(AutoFetchExclusionsForm::class.java)
  }
}
