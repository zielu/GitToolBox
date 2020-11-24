package zielu.intellij.ui;

import com.intellij.openapi.options.BaseConfigurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.util.ui.UIUtil;
import javax.swing.JComponent;
import org.jetbrains.annotations.Nullable;

public abstract class GtConfigurableBase<F extends GtFormUi, C> extends
    BaseConfigurable {

  private volatile F form;

  protected abstract F createForm();

  protected abstract C getConfig();

  protected abstract void setFormState(F form, C config);

  protected C prepareConfigBeforeFormFill(C config) {
    return config;
  }

  protected abstract boolean checkModified(F form, C config);

  protected abstract void doApply(F form, C config) throws ConfigurationException;

  protected void afterInit(F form) {
  }

  protected void dispose() {
  }

  private synchronized F getForm() {
    return form;
  }

  private synchronized void initComponent() {
    if (form == null) {
      form = UIUtil.invokeAndWaitIfNeeded(() -> {
        F newForm = createForm();
        newForm.init();
        afterInit(newForm);
        return newForm;
      });
    }
  }

  @Nullable
  @Override
  public final JComponent createComponent() {
    initComponent();
    F currentForm = fillFormFromConfig();
    return currentForm.getContent();
  }

  private F fillFormFromConfig() {
    F currentForm = getForm();
    C config = prepareConfigBeforeFormFill(getConfig());
    setFormState(currentForm, config);
    currentForm.afterStateSet();
    return currentForm;
  }

  @Override
  public final boolean isModified() {
    setModified(checkModified(getForm(), getConfig()));
    return super.isModified();
  }

  @Override
  public final void apply() throws ConfigurationException {
    initComponent();
    doApply(getForm(), getConfig());
  }

  @Override
  public final void reset() {
    initComponent();
    fillFormFromConfig();
  }

  @Override
  public synchronized void disposeUIResources() {
    dispose();
    if (form != null) {
      form.dispose();
    }
    form = null;
  }
}
