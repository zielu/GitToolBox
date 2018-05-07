package zielu.intellij.ui;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.options.BaseConfigurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.Computable;
import com.intellij.util.ui.UIUtil;
import javax.swing.JComponent;
import org.jetbrains.annotations.Nullable;
import zielu.intellij.ui.GtFormUi;

public abstract class GtConfigurableBase<F extends GtFormUi, C extends PersistentStateComponent> extends
    BaseConfigurable {

  private volatile F form;

  protected abstract F createForm();

  protected abstract C getConfig();

  protected abstract void setFormState(F form, C config);

  protected abstract boolean checkModified(F form, C config);

  protected abstract void doApply(F form, C config) throws ConfigurationException;

  protected void dispose() {
  }

  protected final F getForm() {
    return form;
  }

  private synchronized void initComponent() {
    if (form == null) {
      form = UIUtil.invokeAndWaitIfNeeded(new Computable<F>() {
        @Override
        public F compute() {
          F form = createForm();
          form.init();
          return form;
        }
      });
    }
  }

  @Nullable
  @Override
  public final JComponent createComponent() {
    initComponent();
    F currentForm = getForm();
    setFormState(currentForm, getConfig());
    currentForm.afterStateSet();
    return currentForm.getContent();
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
    setFormState(getForm(), getConfig());
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
