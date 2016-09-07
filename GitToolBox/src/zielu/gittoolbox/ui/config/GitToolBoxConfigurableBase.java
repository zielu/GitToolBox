package zielu.gittoolbox.ui.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.options.BaseConfigurable;
import com.intellij.openapi.options.ConfigurationException;
import javax.swing.JComponent;
import org.jetbrains.annotations.Nullable;

public abstract class GitToolBoxConfigurableBase
    <FORM extends GitToolBoxFormUi, CONFIG extends PersistentStateComponent> extends BaseConfigurable {

    private volatile FORM form;

    protected abstract FORM createForm();
    protected abstract CONFIG getConfig();
    protected abstract void setFormState(FORM form, CONFIG config);
    protected abstract boolean checkModified(FORM form, CONFIG config);
    protected abstract void doApply(FORM form, CONFIG config) throws ConfigurationException;
    protected void dispose() {}

    protected final FORM getForm() {
        return form;
    }

    private synchronized void initComponent() {
        if (form == null) {
            form = createForm();
            form.init();
        }
    }

    @Nullable
    @Override
    public final JComponent createComponent() {
        initComponent();
        setFormState(getForm(), getConfig());
        return form.getContent();
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
        form = null;
    }
}
