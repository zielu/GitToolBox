package zielu.intellij.ui;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.NotNull;

public abstract class GtBinderConfigurableBase<F extends GtFormUi, C> extends
    GtConfigurableBase<F, C> {
  private final Logger log = Logger.getInstance(getClass());
  private final ConfigUiBinder<C, F> binder = new ConfigUiBinder<>();

  protected GtBinderConfigurableBase() {
    bind(binder);
  }

  protected abstract void bind(@NotNull ConfigUiBinder<C, F> binder);

  @Override
  protected final void setFormState(F form, C config) {
    log.debug("Set form state");
    binder.populateUi(config, form);
  }

  @Override
  protected final boolean checkModified(F form, C config) {
    boolean modified = binder.checkModified(config, form);
    log.debug("Modified: ", modified);
    return modified;
  }

  @Override
  protected final void doApply(F form, C config) throws ConfigurationException {
    C previous = copy(config);
    binder.populateConfig(config, form);
    log.debug("Applied");
    afterApply(previous, config);
  }

  @NotNull
  protected abstract C copy(@NotNull C config);

  protected abstract void afterApply(@NotNull C previous, @NotNull C current);
}
