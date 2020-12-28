package zielu.intellij.ui;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.options.BaseConfigurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.ui.UIUtil;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GtConfigurableBase<F extends GtFormUi, C> extends
    BaseConfigurable implements Disposable {

  private final AtomicReference<F> form = new AtomicReference<>();

  @NotNull
  protected abstract F createForm();

  @NotNull
  protected abstract C getConfig();

  protected abstract void setFormState(@NotNull F form, @NotNull C config);

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
    return form.get();
  }

  private synchronized void initComponent() {
    if (form.get() == null) {
      form.compareAndSet(null, UIUtil.invokeAndWaitIfNeeded(() -> {
        F newForm = createForm();
        newForm.init();
        afterInit(newForm);
        return newForm;
      }));
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
    Disposer.dispose(this);
    F formInstance = form.getAndSet(null);
    if (formInstance != null) {
      formInstance.dispose();
    }
  }
}
