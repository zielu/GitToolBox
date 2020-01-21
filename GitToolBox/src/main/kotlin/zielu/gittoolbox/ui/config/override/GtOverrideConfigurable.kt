package zielu.gittoolbox.ui.config.override

import com.intellij.openapi.options.SearchableConfigurable
import zielu.gittoolbox.ResBundle
import zielu.gittoolbox.config.GitToolBoxConfigExtras
import zielu.intellij.ui.ConfigUiBinder
import zielu.intellij.ui.GtBinderConfigurableBase

internal class GtOverrideConfigurable :
  GtBinderConfigurableBase<GtOverrideForm, GitToolBoxConfigExtras>(),
  SearchableConfigurable {

  override fun copy(config: GitToolBoxConfigExtras): GitToolBoxConfigExtras {
    return config.copy()
  }

  override fun createForm(): GtOverrideForm = GtOverrideForm()

  override fun getId(): String = "zielu.gittoolbox.extras.config"

  override fun getDisplayName(): String = ResBundle.message("configurable.extras.displayName")

  override fun bind(binder: ConfigUiBinder<GitToolBoxConfigExtras, GtOverrideForm>) {
    binder.bind(
      { c -> c.autoFetchEnabledOverride.enabled },
      { c, v -> c.autoFetchEnabledOverride.enabled = v },
      { f -> f.autoFetchEnabledOverride },
      { f, v -> f.autoFetchEnabledOverride = v }
    )
    binder.bind(
      { c -> c.autoFetchEnabledOverride.value },
      { c, v -> c.autoFetchEnabledOverride.value = v },
      { f -> f.autoFetchEnabled },
      { f, v -> f.autoFetchEnabled = v }
    )
    binder.bind(
      { c -> c.autoFetchEnabledOverride.getAppliedPaths() },
      { f, v -> f.setAppliedAutoFetchEnabledPaths(v) }
    )

    binder.bind(
      { c -> c.autoFetchOnBranchSwitchOverride.enabled },
      { c, v -> c.autoFetchOnBranchSwitchOverride.enabled = v },
      { f -> f.autoFetchOnBranchSwitchEnabledOverride },
      { f, v -> f.autoFetchOnBranchSwitchEnabledOverride = v }
    )
    binder.bind(
      { c -> c.autoFetchOnBranchSwitchOverride.value },
      { c, v -> c.autoFetchOnBranchSwitchOverride.value = v },
      { f -> f.autoFetchOnBranchSwitchEnabled },
      { f, v -> f.autoFetchOnBranchSwitchEnabled = v }
    )
    binder.bind(
      { c -> c.autoFetchOnBranchSwitchOverride.getAppliedPaths() },
      { f, v -> f.setAppliedAutoFetchOnBranchSwitchEnabledPaths(v) }
    )
  }

  override fun afterApply(previous: GitToolBoxConfigExtras, current: GitToolBoxConfigExtras) {
    // do nothing
  }

  override fun getConfig(): GitToolBoxConfigExtras = GitToolBoxConfigExtras.getInstance()
}
