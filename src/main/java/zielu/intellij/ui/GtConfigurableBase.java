package zielu.intellij.ui;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.options.BaseConfigurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.ui.UIUtil;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GtConfigurableBase<F extends GtFormUi, C> extends
    BaseConfigurable implements Disposable {

  private volatile F form;

  @NotNull
  protected abstract F createForm();

  @NotNull
  protected abstract C getConfig();

  protected abstract void setFormState(@NotNull F form, @NotNull C config);

  @Deprecated(forRemoval = true)
  protected C prepareConfigBeforeFormFill(@NotNull C config) {
    return config;
  }

  protected abstract boolean checkModified(@NotNull F form, @NotNull C config);

  protected abstract void doApply(@NotNull F form, @NotNull C config) throws ConfigurationException;

  protected void afterInit(@NotNull F form) {
  }

  @Override
  public final void dispose() {
    //do nothing
  }

  private synchronized F getForm() {
    if (form == null) {
      synchronized (this) {
        if (form == null) {
          form = UIUtil.invokeAndWaitIfNeeded(() -> {
            F newForm = createForm();
            newForm.init();
            afterInit(newForm);
            Disposer.register(this, newForm);
            return newForm;
          });
        }
      }
    }
    return form;
  }

  @Nullable
  @Override
  public final JComponent createComponent() {
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
    doApply(getForm(), getConfig());
  }

  @Override
  public final void reset() {
    fillFormFromConfig();
  }

  @Override
  public synchronized void disposeUIResources() {
    Disposer.dispose(this);
    form = null;
  }
}
