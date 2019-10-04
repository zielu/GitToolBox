package zielu.intellij.ui;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.ConfigurationException;

public abstract class GtBinderConfigurableBase<F extends GtFormUi, C extends PersistentStateComponent> extends
    GtConfigurableBase<F, C> {
  private final Logger log = Logger.getInstance(getClass());
  private final ConfigUiBinder<C, F> binder = new ConfigUiBinder<>();

  protected GtBinderConfigurableBase() {
    bind(binder);
  }

  protected abstract void bind(ConfigUiBinder<C, F> binder);

  @Override
  protected void setFormState(F form, C config) {
    log.debug("Set form state");
    binder.populateUi(config, form);
  }

  @Override
  protected boolean checkModified(F form, C config) {
    boolean modified = binder.checkModified(config, form);
    log.debug("Modified: ", modified);
    return modified;
  }

  @Override
  protected void doApply(F form, C config) throws ConfigurationException {
    C previous = copy(config);
    binder.populateConfig(config, form);
    log.debug("Applied");
    afterApply(previous, config);
  }

  protected abstract C copy(C config);

  protected abstract void afterApply(C previous, C current);
}
