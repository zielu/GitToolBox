package zielu.gittoolbox.ui.config.override

import com.intellij.openapi.options.SearchableConfigurable
import zielu.gittoolbox.ResBundle
import zielu.gittoolbox.config.GitToolBoxConfigOverride
import zielu.intellij.ui.ConfigUiBinder
import zielu.intellij.ui.GtBinderConfigurableBase
import java.util.function.BiConsumer
import java.util.function.Function

internal class GtOverrideConfigurable :
  GtBinderConfigurableBase<GtOverrideForm, GitToolBoxConfigOverride>(),
  SearchableConfigurable {

  override fun copy(config: GitToolBoxConfigOverride): GitToolBoxConfigOverride {
    return config.copy()
  }

  override fun createForm(): GtOverrideForm = GtOverrideForm()

  override fun getId(): String = "zielu.gittoolbox.override.config"

  override fun getDisplayName(): String = ResBundle.message("configurable.override.displayName")

  override fun bind(binder: ConfigUiBinder<GitToolBoxConfigOverride, GtOverrideForm>) {
    binder.bind(
      Function { c -> c.autoFetchEnabledOverride.enabled },
      BiConsumer { c, v -> c.autoFetchEnabledOverride.enabled = v },
      Function { f -> f.autoFetchEnabledOverride },
      BiConsumer<GtOverrideForm, Boolean> { f, v -> f.autoFetchEnabledOverride = v }
    )
    binder.bind(
      Function { c -> c.autoFetchEnabledOverride.value },
      BiConsumer { c, v -> c.autoFetchEnabledOverride.value = v },
      Function { f -> f.autoFetchEnabled },
      BiConsumer<GtOverrideForm, Boolean> { f, v -> f.autoFetchEnabled = v }
    )
  }

  override fun afterApply(previous: GitToolBoxConfigOverride, current: GitToolBoxConfigOverride) {
    // do nothing
  }

  override fun getConfig(): GitToolBoxConfigOverride = GitToolBoxConfigOverride.getInstance()
}
